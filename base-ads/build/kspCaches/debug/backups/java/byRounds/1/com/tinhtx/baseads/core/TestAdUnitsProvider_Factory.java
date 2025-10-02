package com.tinhtx.baseads.core;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class TestAdUnitsProvider_Factory implements Factory<TestAdUnitsProvider> {
  @Override
  public TestAdUnitsProvider get() {
    return newInstance();
  }

  public static TestAdUnitsProvider_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static TestAdUnitsProvider newInstance() {
    return new TestAdUnitsProvider();
  }

  private static final class InstanceHolder {
    static final TestAdUnitsProvider_Factory INSTANCE = new TestAdUnitsProvider_Factory();
  }
}
