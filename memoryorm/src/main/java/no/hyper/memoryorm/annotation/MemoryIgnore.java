package no.hyper.memoryorm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Created by jean on 24.08.2016.
 */
@Target(value = ElementType.FIELD)
public @interface MemoryIgnore { }
