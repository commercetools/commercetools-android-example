package de.commercetools.android_example;

import javax.inject.Singleton;

import dagger.Component;
import dagger.android.AndroidInjectionModule;

@Singleton
@Component(modules={AndroidInjectionModule.class, AppModule.class, CartActivityModule.class, JsonModule.class})
public interface AppComponent {
    void inject(CtApp app);
}
