package com.tinhtx.baseads.banner;

import com.tinhtx.baseads.core.AdRevenueReporter;
import com.tinhtx.baseads.core.AdUnitsProvider;
import com.tinhtx.baseads.core.AdsConfig;
import com.tinhtx.baseads.core.AdsInitializer;
import com.tinhtx.baseads.core.AnalyticsLogger;
import com.tinhtx.baseads.core.VipGate;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
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
public final class BannerPreloader_Factory implements Factory<BannerPreloader> {
  private final Provider<AdUnitsProvider> adUnitsProvider;

  private final Provider<AdsConfig> adsConfigProvider;

  private final Provider<VipGate> vipGateProvider;

  private final Provider<AnalyticsLogger> analyticsLoggerProvider;

  private final Provider<AdsInitializer> adsInitializerProvider;

  private final Provider<AdRevenueReporter> adRevenueReporterProvider;

  public BannerPreloader_Factory(Provider<AdUnitsProvider> adUnitsProvider,
      Provider<AdsConfig> adsConfigProvider, Provider<VipGate> vipGateProvider,
      Provider<AnalyticsLogger> analyticsLoggerProvider,
      Provider<AdsInitializer> adsInitializerProvider,
      Provider<AdRevenueReporter> adRevenueReporterProvider) {
    this.adUnitsProvider = adUnitsProvider;
    this.adsConfigProvider = adsConfigProvider;
    this.vipGateProvider = vipGateProvider;
    this.analyticsLoggerProvider = analyticsLoggerProvider;
    this.adsInitializerProvider = adsInitializerProvider;
    this.adRevenueReporterProvider = adRevenueReporterProvider;
  }

  @Override
  public BannerPreloader get() {
    return newInstance(adUnitsProvider.get(), adsConfigProvider.get(), vipGateProvider.get(), analyticsLoggerProvider.get(), adsInitializerProvider.get(), adRevenueReporterProvider.get());
  }

  public static BannerPreloader_Factory create(Provider<AdUnitsProvider> adUnitsProvider,
      Provider<AdsConfig> adsConfigProvider, Provider<VipGate> vipGateProvider,
      Provider<AnalyticsLogger> analyticsLoggerProvider,
      Provider<AdsInitializer> adsInitializerProvider,
      Provider<AdRevenueReporter> adRevenueReporterProvider) {
    return new BannerPreloader_Factory(adUnitsProvider, adsConfigProvider, vipGateProvider, analyticsLoggerProvider, adsInitializerProvider, adRevenueReporterProvider);
  }

  public static BannerPreloader newInstance(AdUnitsProvider adUnitsProvider, AdsConfig adsConfig,
      VipGate vipGate, AnalyticsLogger analyticsLogger, AdsInitializer adsInitializer,
      AdRevenueReporter adRevenueReporter) {
    return new BannerPreloader(adUnitsProvider, adsConfig, vipGate, analyticsLogger, adsInitializer, adRevenueReporter);
  }
}
