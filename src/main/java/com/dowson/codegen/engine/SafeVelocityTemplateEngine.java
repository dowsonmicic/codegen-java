package com.dowson.codegen.engine;

import com.baomidou.mybatisplus.generator.config.builder.ConfigBuilder;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;
import org.apache.velocity.app.VelocityEngine;

import java.lang.reflect.Field;
import java.util.Properties;

public class SafeVelocityTemplateEngine extends VelocityTemplateEngine {
    @Override
    public SafeVelocityTemplateEngine init(ConfigBuilder configBuilder) {
        Properties p = new Properties();
        p.setProperty("resource.loaders", "class,file");
        p.setProperty("resource.loader.class.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        p.setProperty("resource.loader.file.class",
                "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        p.setProperty("resource.loader.file.path", "");
        p.setProperty("resource.default_encoding", "UTF-8");
        p.setProperty("output.encoding", "UTF-8");
        p.setProperty("resource.loader.file.unicode", "true");
        VelocityEngine ve = new VelocityEngine(p);
        try {
            Field f = VelocityTemplateEngine.class.getDeclaredField("velocityEngine");
            f.setAccessible(true);
            f.set(this, ve);
        } catch (Exception ignored) {
        }
        return this;
    }
}
