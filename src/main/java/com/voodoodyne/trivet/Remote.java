package com.voodoodyne.trivet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation required on classes that expose remote interfaces; you must explicitly declare that an
 * implementing class exposes remote interfaces.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface Remote {
	/** If you do not specify an interface, all implemented interfaces are considered safe for remoting */
	Class<?>[] value() default {};
}
