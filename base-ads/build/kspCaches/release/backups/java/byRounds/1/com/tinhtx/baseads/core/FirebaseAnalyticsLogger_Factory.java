package com.tinhtx.baseads.core;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
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
public final class FirebaseAnalyticsLogger_Factory implements Factory<FirebaseAnalyticsLogger> {
  @Override
  public FirebaseAnalyticsLogger get() {
    return newInstance();
  }

  public static FirebaseAnalyticsLogger_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FirebaseAnalyticsLogger newInstance() {
    return new FirebaseAnalyticsLogger();
  }

  private static final class InstanceHolder {
    static final FirebaseAnalyticsLogger_Factory INSTANCE = new FirebaseAnalyticsLogger_Factory();
  }
}
