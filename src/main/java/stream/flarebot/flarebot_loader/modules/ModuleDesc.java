package stream.flarebot.flarebot_loader.modules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface ModuleDesc {

    String name();

    String id();

    boolean core() default false;

    String version();
}