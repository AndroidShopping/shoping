package com.shop.shopping;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.load.engine.executor.GlideExecutor;
import com.bumptech.glide.module.GlideModule;

public class GlideConfig implements GlideModule {
    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        builder.setSourceExecutor(GlideExecutor.newSourceExecutor(1, "MyGlide", new GlideExecutor.UncaughtThrowableStrategy() {
            @Override
            public void handle(Throwable t) {
                Log.d("GlideConfig", "handle: ");
                t.printStackTrace();
            }
        }));


    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {

    }
}
