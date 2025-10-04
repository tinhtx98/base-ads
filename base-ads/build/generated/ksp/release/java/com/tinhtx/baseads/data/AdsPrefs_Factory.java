package com.tinhtx.baseads.data;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class AdsPrefs_Factory implements Factory<AdsPrefs> {
  private final Provider<Context> contextProvider;

  public AdsPrefs_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public AdsPrefs get() {
    return newInstance(contextProvider.get());
  }

  public static AdsPrefs_Factory create(Provider<Context> contextProvider) {
    return new AdsPrefs_Factory(contextProvider);
  }

  public static AdsPrefs newInstance(Context context) {
    return new AdsPrefs(context);
  }
}
