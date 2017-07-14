package de.commercetools.android_example;

import android.app.Activity;

import dagger.Binds;
import dagger.Module;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.multibindings.IntoMap;

@Module(subcomponents = CartActivityComponent.class)
public abstract class CartActivityModule {

    @Binds
    @IntoMap
    @ActivityKey(CartActivity.class)
    abstract AndroidInjector.Factory<? extends Activity>
    bindCartActivityInjectorFactory(CartActivityComponent.Builder builder);
}
