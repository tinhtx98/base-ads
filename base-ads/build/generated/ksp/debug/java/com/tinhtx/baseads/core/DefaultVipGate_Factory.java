package com.tinhtx.baseads.core;

import com.tinhtx.baseads.data.AdsPrefs;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
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
public final class DefaultVipGate_Factory implements Factory<DefaultVipGate> {
  private final Provider<AdsPrefs> adsPrefsProvider;

  public DefaultVipGate_Factory(Provider<AdsPrefs> adsPrefsProvider) {
    this.adsPrefsProvider = adsPrefsProvider;
  }

  @Override
  public DefaultVipGate get() {
    return newInstance(adsPrefsProvider.get());
  }

  public static DefaultVipGate_Factory create(Provider<AdsPrefs> adsPrefsProvider) {
    return new DefaultVipGate_Factory(adsPrefsProvider);
  }

  public static DefaultVipGate newInstance(AdsPrefs adsPrefs) {
    return new DefaultVipGate(adsPrefs);
  }
}
