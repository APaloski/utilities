package com.paloski.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A marker annotation that indicates that a method or constructor is called by reflection.
 * <p/>
 * While it is normally advisable that reflective calls be avoided, or that the annotations
 * provided
 * by the tool should be preferred (e.g. Gson @SerializedName annotations), this annotation is
 * useful in cases such as a required library that uses reflection that is not directly referenced (e.g.
 * {@link Class#newInstance()} or the default constructor required by {@link java.io.Externalizable}).
 * <p/>
 * When using this annotation, you should add it to tools such as IntelliJs inspections to avoid unused warnings.
 *
 * @author Adam
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Documented
public @interface CalledByReflection {

	/**
	 * The class(es) that invokes this method, or {@link Void} if the class is unknown, does not
	 * exist on the classpath, or is controlled by XML.
	 */
	Class<?>[] value();
}
