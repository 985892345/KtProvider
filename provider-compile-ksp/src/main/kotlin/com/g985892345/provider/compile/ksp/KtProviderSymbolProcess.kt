package com.g985892345.provider.compile.ksp

import com.g985892345.provider.api.annotation.ImplProvider
import com.g985892345.provider.api.annotation.KClassProvider
import com.g985892345.provider.api.init.IKtProviderDelegate
import com.g985892345.provider.api.init.KtProviderRouter
import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import kotlin.reflect.KClass

/**
 * .
 *
 * @author 985892345
 * 2023/12/4 14:21
 */
class KtProviderSymbolProcess(
  private val logger: KSPLogger,
  private val codeGenerator: CodeGenerator,
  private val options: Options,
) : SymbolProcessor {
  
  override fun process(resolver: Resolver): List<KSAnnotated> {
    val implProviderMap = findImplProvider(resolver)
    val kClassProviderMap = findKClassProvider(resolver)
    generateKtProvider(
      buildMap<KSFile, MutableList<AddFunStatement>> {
        implProviderMap.forEach { getOrPut(it.key) { mutableListOf() }.add(it.value) }
        kClassProviderMap.forEach { getOrPut(it.key) { mutableListOf() }.add(it.value) }
      }
    )
    return emptyList()
  }
  
  private fun findImplProvider(resolver: Resolver): Map<KSFile, AddFunStatement> {
    return resolver.getSymbolsWithAnnotation(ImplProvider::class.qualifiedName!!)
      .filterIsInstance<KSClassDeclaration>()
      .filter {
        it.classKind == ClassKind.CLASS || it.classKind == ClassKind.OBJECT
      }.map { ImplProviderStatement(it) }
      .associateBy { it.declaration.containingFile!! }
  }
  
  private fun findKClassProvider(resolver: Resolver): Map<KSFile, AddFunStatement> {
    return resolver.getSymbolsWithAnnotation(KClassProvider::class.qualifiedName!!)
      .filterIsInstance<KSClassDeclaration>()
      .filter {
        it.classKind == ClassKind.CLASS || it.classKind == ClassKind.OBJECT || it.classKind == ClassKind.INTERFACE
      }.map { KClassProviderStatement(it) }
      .associateBy { it.declaration.containingFile!! }
  }
  
  private fun generateKtProvider(data: Map<KSFile, List<AddFunStatement>>) {
    FileSpec.builder(options.packageName, options.className)
      .addType(
        TypeSpec.objectBuilder(options.className)
          .addModifiers(KModifier.INTERNAL)
          .superclass(KtProviderRouter::class)
          .addFunction(
            FunSpec.builder(KtProviderRouter::initRouter.name)
              .addModifiers(KModifier.OVERRIDE)
              .addParameter("delegate", IKtProviderDelegate::class)
              .apply {
                data.values.flatten().forEach {
                  it.addStatement(this)
                }
              }.build()
          ).build()
      ).build().apply {
        try {
          writeTo(codeGenerator, true, data.keys)
        } catch (e: FileAlreadyExistsException) {
          // An exception will be thrown when generating the KtProviderRouter implementation class repeatedly,
          // but it cannot be determined by whether the "data" is empty because it could be the second generation.
          // When data is empty, it is also necessary to generate the KtProviderRouter implementation class.
        }
      }
  }
  
  private interface AddFunStatement {
    fun addStatement(builder: FunSpec.Builder)
  }
  
  private inner class ImplProviderStatement(
    val declaration: KSClassDeclaration
  ) : AddFunStatement {
    @OptIn(KspExperimental::class)
    override fun addStatement(builder: FunSpec.Builder) {
      declaration.getAnnotationsByType(ImplProvider::class)
        .forEach {
          val clazzClassName = getClazzTypeName(declaration, it.name) { it.clazz }
          logger.warn("declaration = ${declaration.toClassName()}, clazzClassName = $clazzClassName")
          if (clazzClassName != null) {
            if (declaration.classKind == ClassKind.OBJECT) {
              builder.addStatement(
                "delegate.addImplProvider(%T::class, %S) { %T }",
                clazzClassName, it.name, declaration.toClassName()
              )
            } else {
              builder.addStatement(
                "delegate.addImplProvider(%T::class, %S, ::%T)",
                clazzClassName, it.name, declaration.toClassName()
              )
            }
          } else {
            if (declaration.classKind == ClassKind.OBJECT) {
              builder.addStatement(
                "delegate.addImplProvider(null, %S) { %T }",
                it.name, declaration.toClassName()
              )
            } else {
              builder.addStatement(
                "delegate.addImplProvider(null, %S, ::%T)",
                it.name, declaration.toClassName()
              )
            }
          }
        }
    }
  }
  
  private inner class KClassProviderStatement(
    val declaration: KSClassDeclaration
  ) : AddFunStatement {
    @OptIn(KspExperimental::class)
    override fun addStatement(builder: FunSpec.Builder) {
      declaration.getAnnotationsByType(KClassProvider::class)
        .forEach {
          val clazzClassName = getClazzTypeName(declaration, it.name) { it.clazz }
          if (clazzClassName != null) {
            builder.addStatement(
              "delegate.addImplProvider(%T::class, %S) { %T::class }",
              clazzClassName, it.name, declaration.toClassName()
            )
          } else {
            builder.addStatement(
              "delegate.addImplProvider(null, %S) { %T::class }",
              it.name, declaration.toClassName()
            )
          }
        }
    }
  }
  
  @OptIn(KspExperimental::class)
  private fun getClazzTypeName(declaration: KSClassDeclaration, name: String, getClazz: () -> KClass<*>): TypeName? {
    return try {
      val clazz = getClazz.invoke()
      if (clazz == Void::class || clazz == Nothing::class) null else clazz.asClassName()
    } catch (e: KSTypeNotPresentException) {
      e.ksType.toClassName()
    } catch (e: NoSuchElementException) {
      // Retrieving the default value of clazz in Native will fail here.
      null
    }.let {
      if (it == null && name.isEmpty()) {
        val superTypesSize = declaration.superTypes.count()
        if (superTypesSize == 0) {
          throw IllegalArgumentException("${declaration.simpleName} unimplemented clazz. " +
              "The position is as follows: ${declaration.location}")
        } else if (superTypesSize != 1) {
          throw IllegalArgumentException("It is only allowed to omit clazz and name " +
              "when the parent type has only one interface or class. " +
              "The position is as follows: ${declaration.location}")
        }
        declaration.superTypes.first().toTypeName()
      } else it
    }
  }
}