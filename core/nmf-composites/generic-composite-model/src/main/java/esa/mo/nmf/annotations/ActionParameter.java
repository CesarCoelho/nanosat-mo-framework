/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esa.mo.nmf.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.ccsds.moims.mo.mc.structures.ConditionalConversionList;

/**
 *
 * @author Kevin Otto <Kevin@KevinOtto.de>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ActionParameter
{

  String name() default "";

  String description() default "";

  byte rawType() default 0;

  String rawUnit() default "";
  ConditionalConversionList conditionalConversions = null;
  Byte convertedType = null;
  String convertedUnit = null;
}
