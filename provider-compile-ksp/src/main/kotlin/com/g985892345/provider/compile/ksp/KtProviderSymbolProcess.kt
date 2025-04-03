package com.g985892345.provider.compile.ksp

import com.g985892345.provider.api.annotation.ImplProvider
import com.g985892345.provider.api.annotation.KClassProvider
import com.g985892345.provider.api.init.IKtProviderDelegate
import com.g985892345.provider.api.init.KtProviderInitializer
import com.g985892345.provider.api.init.KtProviderRouter
import com.google.devtools.ksp.KSTypeNotPresentException
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getKotlinClassByName
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
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
  
  private var processNowCount = 1
  
  private var lastKtProviderRouterClassName: ClassName? = null
  
  override fun process(resolver: Resolver): List<KSAnnotated> {
    if (processNowCount > options.processMaxCount) return emptyList()
    if (processNowCount == 1) {
      log("dependModuleProjects = " + options.dependModuleProjects.joinToString("\n"))
      generateKtProviderInitializer(resolver)
    }
    val implProviderMap = findImplProvider(resolver)
    val kClassProviderMap = findKClassProvider(resolver)
    val statements = implProviderMap + kClassProviderMap
    generateKtProviderRouter(processNowCount, statements)
    processNowCount++
    return emptyList()
  }
  
  @OptIn(KspExperimental::class)
  private fun generateKtProviderInitializer(resolver: Resolver) {
    val otherModuleInitializers = options.dependModuleProjects.mapNotNull {
      resolver.getKotlinClassByName(it)
    }
    FileSpec.builder(options.initializerPackageName, options.initializerClassName)
      .addType(
        TypeSpec.objectBuilder(options.initializerClassName)
          .superclass(KtProviderInitializer::class)
          .addProperty(
            PropertySpec.builder("router", KtProviderRouter::class)
              .addModifiers(KModifier.OVERRIDE)
              .initializer("getRouter()")
              .build()
          )
          .addProperty(
            PropertySpec.builder("otherModuleKtProvider", typeNameOf<List<KtProviderInitializer>>())
              .addModifiers(KModifier.OVERRIDE)
              .apply {
                if (otherModuleInitializers.isEmpty()) {
                  initializer("emptyList()")
                } else {
                  initializer(
                    "listOf(${otherModuleInitializers.joinToString { "%T" }})",
                    *otherModuleInitializers.map { it.toClassName() }.toTypedArray())
                }
              }
              .build()
          )
          .build()
      ).addFunction(
        FunSpec.builder("getRouter")
          .addModifiers(KModifier.PRIVATE)
          .returns(KtProviderRouter::class)
          .receiver(ClassName(options.initializerPackageName, options.initializerClassName))
          .addStatement("return KtProviderRouter.Empty")
          .addAnnotation(
            AnnotationSpec.builder(Suppress::class)
              .addMember("%S", "UNUSED_PARAMETER")
              .addMember("%S", "UnusedReceiverParameter")
              .build()
          )
          .apply {
            repeat(options.processMaxCount + 1) {
              addParameter(
                ParameterSpec.builder("a$it", Unit::class)
                  .defaultValue("Unit")
                  .build()
              )
            }
          }.build()
      )
      .build().writeTo(
        codeGenerator,
        true,
        otherModuleInitializers.mapNotNull { it.containingFile }
      )
  }
  
  private fun findImplProvider(resolver: Resolver): List<AddFunStatement> {
    return resolver.getSymbolsWithAnnotation(ImplProvider::class.qualifiedName!!)
      .filterIsInstance<KSClassDeclaration>()
      .filter {
        it.classKind == ClassKind.CLASS || it.classKind == ClassKind.OBJECT || it.classKind == ClassKind.ENUM_ENTRY
      }.map { ImplProviderStatement(it) }.toList()
  }
  
  private fun findKClassProvider(resolver: Resolver): List<AddFunStatement> {
    return resolver.getSymbolsWithAnnotation(KClassProvider::class.qualifiedName!!)
      .filterIsInstance<KSClassDeclaration>()
      .filter {
        it.classKind == ClassKind.CLASS || it.classKind == ClassKind.OBJECT || it.classKind == ClassKind.INTERFACE
      }.map { KClassProviderStatement(it) }.toList()
  }
  
  private fun getKtProviderRouterName(processTimes: Int): String {
    return "_${options.className}_${processTimes - 1}"
  }
  
  private fun generateKtProviderRouter(
    processNowTimes: Int,
    data: List<AddFunStatement>,
  ) {
    if (data.isEmpty()) return
    val ktProviderRouterName = getKtProviderRouterName(processNowTimes)
    FileSpec.builder(options.packageName, ktProviderRouterName)
      .addType(
        TypeSpec.objectBuilder(ktProviderRouterName)
          .addModifiers(KModifier.INTERNAL)
          .superclass(KtProviderRouter::class)
          .addFunction(
            FunSpec.builder(KtProviderRouter::initRouter.name)
              .addModifiers(KModifier.OVERRIDE)
              .addParameter("delegate", IKtProviderDelegate::class)
              .apply {
                lastKtProviderRouterClassName?.let {
                  addStatement("%T.initRouter(delegate)", it)
                }
                data.forEach {
                  it.addStatement(this)
                }
              }.build()
          ).build()
      ).addFunction(
        FunSpec.builder("getRouter")
          .addModifiers(KModifier.INTERNAL)
          .returns(KtProviderRouter::class)
          .receiver(ClassName(options.initializerPackageName, options.initializerClassName))
          .addStatement("return $ktProviderRouterName")
          .apply {
            repeat(options.processMaxCount - processNowTimes + 1) {
              addParameter(
                ParameterSpec.builder("a$it", Unit::class)
                  .defaultValue("%T", Unit::class.asClassName())
                  .build()
              )
            }
          }
          .addAnnotation(
            AnnotationSpec.builder(Suppress::class)
              .addMember("%S", "UNUSED_PARAMETER")
              .addMember("%S", "UnusedReceiverParameter")
              .build()
          )
          .build()
      ).build().writeTo(codeGenerator, true, data.mapNotNullTo(hashSetOf()) { it.file })
    lastKtProviderRouterClassName = ClassName(options.packageName, ktProviderRouterName)
  }
  
  fun log(msg: String) {
    if (options.logEnable) {
      logger.warn("[KtProvider] $msg")
    }
  }
  
  private interface AddFunStatement {
    val file: KSFile?
    fun addStatement(builder: FunSpec.Builder)
  }
  
  private inner class ImplProviderStatement(
    val declaration: KSClassDeclaration
  ) : AddFunStatement {
    override val file: KSFile?
      get() = declaration.containingFile
    
    @OptIn(KspExperimental::class)
    override fun addStatement(builder: FunSpec.Builder) {
      declaration.getAnnotationsByType(ImplProvider::class)
        .forEach {
          val clazzClassName = getClazzTypeName(declaration, it.name) { it.clazz }
          log("@ImplProvider: declaration = ${declaration.toClassName()}, clazz = $clazzClassName, name = ${it.name}")
          if (clazzClassName != null) {
            if (declaration.classKind == ClassKind.OBJECT || declaration.classKind == ClassKind.ENUM_ENTRY) {
              builder.addStatement(
                "delegate.addImplProvider(%T::class, %S) { %T }",
                clazzClassName, it.name, declaration.toClassName()
              )
            } else {
              builder.addStatement(
                "delegate.addImplProvider(%T::class, %S) { %T() }",
                clazzClassName, it.name, declaration.toClassName()
              )
            }
          } else {
            if (declaration.classKind == ClassKind.OBJECT || declaration.classKind == ClassKind.ENUM_ENTRY) {
              builder.addStatement(
                "delegate.addImplProvider(null, %S) { %T }",
                it.name, declaration.toClassName()
              )
            } else {
              builder.addStatement(
                "delegate.addImplProvider(null, %S) { %T() }",
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
    override val file: KSFile?
      get() = declaration.containingFile
    
    @OptIn(KspExperimental::class)
    override fun addStatement(builder: FunSpec.Builder) {
      declaration.getAnnotationsByType(KClassProvider::class)
        .forEach {
          val clazzClassName = getClazzTypeName(declaration, it.name) { it.clazz }
          log("@KClassProvider: declaration = ${declaration.toClassName()}, clazz = $clazzClassName, name = ${it.name}")
          if (clazzClassName != null) {
            builder.addStatement(
              "delegate.addKClassProvider(%T::class, %S) { %T::class }",
              clazzClassName, it.name, declaration.toClassName()
            )
          } else {
            builder.addStatement(
              "delegate.addKClassProvider(null, %S) { %T::class }",
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
      // %T::class does not require generics, so ksType.toTypeName() cannot be used.
      (e.ksType.declaration as? KSClassDeclaration)?.toClassName() ?: return null
    } catch (e: NoSuchElementException) {
      // Retrieving the default value of clazz in Native will fail here.
      null
    }.let {
      if (it == null && name.isEmpty()) {
        val superTypesSize = declaration.superTypes.count()
        if (superTypesSize == 0) {
          throw IllegalArgumentException(
            "${declaration.simpleName} unimplemented clazz. " +
                "The position is as follows: ${declaration.location}"
          )
        } else if (superTypesSize != 1) {
          throw IllegalArgumentException(
            "It is only allowed to omit clazz and name " +
                "when the parent type has only one interface or class. " +
                "The position is as follows: ${declaration.location}"
          )
        }
        // // %T::class does not require generics, so ksType.toTypeName() cannot be used.
        (declaration.superTypes.first().resolve().declaration as? KSClassDeclaration)?.toClassName()
      } else it
    }
  }
}