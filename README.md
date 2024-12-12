# Java Locales

Library that centralizes and standardizes the use of Unicode locales in Java components.

## Support

Create a [new issue](https://github.com/spotify/java-locales/issues/new)

## Contributing

We feel that a welcoming community is important and we ask that you follow Spotify's
[Open Source Code of Conduct](https://github.com/spotify/code-of-conduct/blob/main/code-of-conduct.md)
in all interactions with the community.

## Authors

* @ericfjosne <efjosne@spotify.com>
* @jsahleen <jsahleen@spotify.com>

A full list of [contributors](https://github.com/spotify/java-locales/graphs/contributors?type=a) can be found on GHC

Follow [@SpotifyEng](https://x.com/spotifyeng) on X for updates.

## Project Description

This open-source library was created to ensure a consistent and culturally relevant localized
end-user experience, by leveraging Unicode locale data and standardized internationalization
algorithms, across software components in a microservices architecture.

Its main purpose is to make sure that the code will leverage the best matching localized end-user
experience using the right [locales available in CLDR](https://cldr.unicode.org/), meaning:

- the best matching supported locale for translations
- the possible supported fallback locales for translations, compatible for combined usage with the
  best matching one
- the best matching locale for formatting, compatible for combined usage with the selected locale
  for translations (best matching or fallback)

This requires that the code is properly internationalized, meaning it must adapt in an automated and
generic way to a given locale.

### Features

This library offers the following 2 main features:

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

### Utility helpers

It also offers utility classes to deal with specific problems:

- [AcceptLanguageUtils](./locales-utils/src/main/java/com/spotify/i18n/locales/utils/acceptlanguage/AcceptLanguageUtils.java):
  Parse and/or normalize raw Accept-Language header values
- [LanguageTagUtils](./locales-utils/src/main/java/com/spotify/i18n/locales/utils/acceptlanguage/LanguageTagUtils.java):
  Parse and/or normalize raw language tags
- [LocalesHierarchyUtils](./locales-utils/src/main/java/com/spotify/i18n/locales/utils/acceptlanguage/LocalesHierarchyUtils.java):
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

We are currently seeking feedback on the internal resolution logic, to ensure that it can expand to
all the world's languages. We also welcome contributions, as long as they remain use-case generic
and benefit all users of the library.

## Develop

This Java library is compiled to run on Java 11 JDK or more recent. Any contribution must ensure
full compatibility with Java 11.

## License

Copyright 2024 Spotify, Inc.

Licensed under the Apache License, Version 2.0: https://www.apache.org/licenses/LICENSE-2.0

## Security Issues?

Please report sensitive security issues via Spotify's bug-bounty
program (https://hackerone.com/spotify) rather than GitHub.

