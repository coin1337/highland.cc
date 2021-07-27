package me.zeroeightsix.kami.module;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Module$Info {
   String name();

   String description() default "Descriptionless";

   Module$Category category();

   boolean alwaysListening() default false;
}
