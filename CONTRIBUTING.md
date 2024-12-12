# How to Contribute

We'd love to get patches from you!

## Getting Started

## Building the Project

`mvn clean verify`

## Workflow

We follow the [GitHub Flow Workflow](https://guides.github.com/introduction/flow/)

1. Fork the project
1. Check out the `main` branch
1. Create a feature branch
1. Write code and tests for your change
1. From your branch, make a pull request against `https://github.com/spotify/java-locales-oss/main`
1. Work with repo maintainers to get your change reviewed
1. Wait for your change to be pulled into `https://github.com/spotify/java-locales-oss/main`
1. Delete your feature branch

## Testing

Change the version with `mvn versions:set -DnewVersion=x.y.z-SNAPSHOT`, do a `mvn install` and then
use `x.y.z-SNAPSHOT` as the new plugin version in your other Maven project (or this project).

## Style

This project uses the `google-java-format`.

## Issues

When creating an issue please try to adhere to the following format:

    module-name: One line summary of the issue (less than 72 characters)

    ### Expected behavior

    As concisely as possible, describe the expected behavior.

    ### Actual behavior

    As concisely as possible, describe the observed behavior.

    ### Steps to reproduce the behavior

    List all relevant steps to reproduce the observed behavior.

## Pull Requests

Before opening a pull request, make sure `mvn verify` runs successfully.

Comments should be formatted to a width no greater than 80 columns.

Files should be exempt of trailing spaces.

We adhere to a specific format for commit messages. Please write your commit
messages along these guidelines. Please keep the line width no greater than 80
columns (You can use `fmt -n -p -w 80` to accomplish this).

    module-name: One line description of your change (less than 72 characters)

    Problem

    Explain the context and why you're making that change.  What is the problem
    you're trying to solve? In some cases there is not a problem and this can be
    thought of being the motivation for your change.

    Solution

    Describe the modifications you've done.

    Result

    What will change as a result of your pull request? Note that sometimes this
    section is unnecessary because it is self-explanatory based on the solution.

Some important notes regarding the summary line:

* Describe what was done; not the result
* Use the active voice
* Use the present tense
* Capitalize properly
* Do not end in a period — this is a title/subject
* Prefix the subject with its scope

If there is already a GitHub issue for the task you are working on, leave a comment to let people
know that you are working on it. If there isn't already an issue, and it is a non-trivial task, it
is a good idea to create one (and note that you're working on it). This prevents contributors from
duplicating effort.

## Code Review

Branch protection is set up to require one approving review from maintainers of this repo.

The repository on GitHub is kept in sync with an internal repository at
Spotify. For the most part this process should be transparent to the project users,
but it does have some implications for how pull requests are merged into the
codebase.

When you submit a pull request on GitHub, it will be reviewed by the project
community (both inside and outside of Spotify), and once the changes are
approved, your commits will be brought into Spotify's internal system for
additional testing. Once the changes are merged internally, they will be pushed
back to GitHub with the next sync.

This process means that the pull request will not be merged in the usual way.
Instead, a member of the project team will post a message in the pull request
thread when your changes have made their way back to GitHub, and the pull
request will be closed.

The changes in the pull request will be collapsed into a single commit, but the
authorship metadata will be preserved.

## Documentation

See [README](README.md)

We also welcome improvements to the project documentation or to the existing
docs. Please file an [issue](https://$REPOURL/issues/New).

# License

By contributing your code, you agree to license your contribution under the
terms of the [LICENSE](https://$LINKTOLICENSEFILE)

# Code of Conduct

Read our [Code of Conduct](CODE_OF_CONDUCT.md) for the project.
