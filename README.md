# Java Locales

Library that centralizes and standardizes the use of Unicode locales in Java components.

## Support

Create a [new issue](https://github.com/spotify/java-locales/issues/new)

## Contributing

We feel that a welcoming community is important and we ask that you follow Spotify's
[Open Source Code of Conduct](https://github.com/spotify/code-of-conduct/blob/master/code-of-conduct.md)
in all interactions with the community.

## Authors

* @ericfjosne <efjosne@spotify.com>
* @jsahleen <jsahleen@spotify.com>

A full list of [contributors](https://github.com/spotify/java-locales/graphs/contributors?type=a)
can be found on GHC

Follow [@SpotifyEng](https://x.com/spotifyeng) on X for updates.

## Project Description

This open-source library was created to ensure a consistent and culturally relevant localized
end-user experience, by leveraging Unicode locale data and standardized internationalization
algorithms, across software components in a microservices architecture.

Its main purpose is to make sure that the code will seamlessly leverage the best matching localized
end-user experience using the right [locales available in CLDR](https://cldr.unicode.org/), meaning:

- the best matching supported locale for translations
- the possible supported fallback locales for translations, compatible for combined usage with the
  best matching one
- the best matching locale for formatting, compatible for combined usage with the selected locale
  for translations (best matching or fallback)

This requires that the code is properly internationalized, meaning it must adapt in an automated and
generic way to a given locale.

### Features

#### Resolve/negotiate locales

This feature enables you to get the best matching locales as described above, for a given list of
supported locales. Essentially, this requires the following 2 logics:

- **Supply the list of supported locales:** For a given product, we defined a supported locales as
  the ones the product is available for. This means locales for which translations are readily
  available.
- **Perform locale resolution:** This is the operation of picking the best matching locales as
  described above, for a given context, context being an abstract parameterized concept which can be
  tailored to your application needs.

It also enables **audience segmentation**, to expose different audiences to different lists of
supported locales. This is convenient when you want to expose locales that have been approved for
production to all end-users, but expose locales being actively developed or checked for quality, to
localization quality assurance testers only.

You can see all these concepts in action
in [our HTTP server example implementation](./examples/locales-http-examples).

#### Calculate the affinity between locales

This feature enables you to easily and programmatically reason around affinity between locales,
without having to know anything about how they relate to each other.

We define the affinity between two locales using a `LocaleAffinity` enum value:

- `NONE`: Locales are totally unrelated
- `LOW`: Locales are somewhat related, meaning they either have low similarities from a linguistic
  perspective or co-exist in given geopolitical or cultural contexts.
- `HIGH`: Locales are quite related, meaning they have similarities from a linguistic perspective.
- `MUTUALLY_INTELLIGIBLE`: Locales identify languages that are similar to a point where a person
  should understand both if they understand one of them.
- `SAME`: Locales identify the same language

We offer separate affinity logics, each dedicated to separate use-cases:

##### Calculate the affinity of a given locale against a set of locales

This should be used when we need visibility on the affinity of a given locale, against a set of
pre-configured locales. This can, for instance, be used to verify whether some content language is a
good match for a given user, based on the Accept-Language header value received in an incoming
request.

You can see this concept in action
in [our example implementation](./examples/locales-affinity-examples/src/main/java/com/spotify/i18n/locales/affinity/examples/AffinityCalculationExampleMain.java).

##### Calculate the affinity between 2 given locales

This should be used when we need visibility on the affinity between two given locales. This can, for
instance, be used to join two datasets based on language identifiers and how they related to each
other in terms of affinity.

It is indeed impossible to perform such a join operation out of the box, as language identifiers
can immensely differ even when they are syntactically valid and identify the very same language. For
example: `zh-Hant`, `zh-HK`, `zh-MO`, `zh-Hant-TW`, `zh-Hant-FR`, `zh-US` all
identify Traditional Chinese, but `zh` and `zh-CN` identify Simplified Chinese.

You can see this concept in action
in [our example implementation](./examples/locales-affinity-examples/src/main/java/com/spotify/i18n/locales/affinity/examples/AffinityBasedJoinExampleMain.java).

### Utility helpers

It also offers utility classes to deal with specific problems:

- [AcceptLanguageUtils](./locales-utils/src/main/java/com/spotify/i18n/locales/utils/acceptlanguage/AcceptLanguageUtils.java):
  Parse and/or normalize raw Accept-Language header values
- [AvailableLocalesUtils](./locales-utils/src/main/java/com/spotify/i18n/locales/utils/available/AvailableLocalesUtils.java):
  Retrieve specific sets of locales
- [LanguageUtils](./locales-utils/src/main/java/com/spotify/i18n/locales/utils/language/LanguageUtils.java):
  Retrieve the best matching written or spoken language locale
- [LanguageTagUtils](./locales-utils/src/main/java/com/spotify/i18n/locales/utils/languagetag/LanguageTagUtils.java):
  Parse and/or normalize raw language tags
- [LocalesHierarchyUtils](./locales-utils/src/main/java/com/spotify/i18n/locales/utils/hierarchy/LocalesHierarchyUtils.java):
  Navigate the locales tree hierarchy, as per [CLDR](https://cldr.unicode.org/).

## Project Direction

### At Spotify

This project is actively maintained at Spotify, where we implemented the concept of supported
locales scope on top of it, as closed source.

In short, a supported locales scope identifies an end-user product or use-case, and the list of
supported locales for this product can be derived from it. This identifier is used as part of the
configuration of services implementing the Java Locales library, and enables us to centrally
maintain the configuration of all our products' supported locales.

This took us from manually, fragmented, loosely coordinated localization initiatives, to
centralized, automated and scalable ones.

By combining a centralized definition of supported locales with a standardized locale resolution
logic, we are now able to provide a more consistent and culturally relevant experience for our
end-users.

### Future developments

We are currently seeking feedback on the internal resolution and affinity logics, to ensure that
they can expand to all the world's languages in a reliable and scalable way. We also welcome
contributions, as long as they remain use-case generic and benefit all users of the library.

## Develop

This Java library is compiled to run on Java 11 JDK or more recent. Any contribution must ensure
full compatibility with Java 11.

## License

Copyright 2024 Spotify, Inc.

Licensed under the Apache License, Version 2.0: https://www.apache.org/licenses/LICENSE-2.0

## Security Issues?

Please report sensitive security issues via Spotify's bug-bounty
program (https://hackerone.com/spotify) rather than GitHub.

