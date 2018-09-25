//package com.maths22.ftcmanuals.config;
//
//import com.maths22.ftcmanuals.models.GameManualSource;
//
//public class AdministrationConfig extends AdministrationConfiguration<GameManualSource> {
//
//    public EntityMetadataConfigurationUnit configuration(EntityMetadataConfigurationUnitBuilder configurationBuilder) {
//        return configurationBuilder.nameField("firstname").build();
//    }
//
//    public ScreenContextConfigurationUnit screenContext(ScreenContextConfigurationUnitBuilder screenContextBuilder) {
//        return screenContextBuilder
//                .screenName("Users Administration").build();
//    }
//
//    public static FieldSetConfigurationUnit listView(final FieldSetConfigurationUnitBuilder fragmentBuilder) {
//        return fragmentBuilder
//                .field("firstname").caption("First Name")
//                .field("lastname").caption("Last Name")
//                .build();
//    }
//}
