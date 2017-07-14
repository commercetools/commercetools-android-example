package de.commercetools.android_example;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@Subcomponent
public interface CartActivityComponent extends AndroidInjector<CartActivity> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<CartActivity> {
    }
}
