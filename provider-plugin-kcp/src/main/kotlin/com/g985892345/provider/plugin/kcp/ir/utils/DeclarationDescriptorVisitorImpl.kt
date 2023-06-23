package com.g985892345.provider.plugin.kcp.ir.utils

import org.jetbrains.kotlin.descriptors.*

/**
 * .
 *
 * @author 985892345
 * 2023/6/15 17:32
 */
open class DeclarationDescriptorVisitorImpl : DeclarationDescriptorVisitor<Unit, Unit> {
  
  override fun visitPackageFragmentDescriptor(descriptor: PackageFragmentDescriptor, data: Unit) {
  }
  
  override fun visitPackageViewDescriptor(descriptor: PackageViewDescriptor, data: Unit) {
  }
  
  override fun visitVariableDescriptor(descriptor: VariableDescriptor, data: Unit) {
  }
  
  override fun visitFunctionDescriptor(descriptor: FunctionDescriptor, data: Unit) {
  }
  
  override fun visitTypeParameterDescriptor(descriptor: TypeParameterDescriptor, data: Unit) {
  }
  
  override fun visitClassDescriptor(descriptor: ClassDescriptor, data: Unit) {
  }
  
  override fun visitTypeAliasDescriptor(descriptor: TypeAliasDescriptor, data: Unit) {
  }
  
  override fun visitModuleDeclaration(descriptor: ModuleDescriptor, data: Unit) {
  }
  
  override fun visitConstructorDescriptor(constructorDescriptor: ConstructorDescriptor, data: Unit) {
  }
  
  override fun visitScriptDescriptor(scriptDescriptor: ScriptDescriptor, data: Unit) {
  }
  
  override fun visitPropertyDescriptor(descriptor: PropertyDescriptor, data: Unit) {
  }
  
  override fun visitValueParameterDescriptor(descriptor: ValueParameterDescriptor, data: Unit) {
  }
  
  override fun visitPropertyGetterDescriptor(descriptor: PropertyGetterDescriptor, data: Unit) {
  }
  
  override fun visitPropertySetterDescriptor(descriptor: PropertySetterDescriptor, data: Unit) {
  }
  
  override fun visitReceiverParameterDescriptor(descriptor: ReceiverParameterDescriptor, data: Unit) {
  }
}