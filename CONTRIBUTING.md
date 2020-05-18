#Contributing Guidelines

Thanks for your interest in Fonda. Our goal is to offer a scalable and automatic analysis of multiple NGS sequencing data types.

- [Branches management guidelines](#branches-management-guidelines)
- [CI process description](#ci-process-description)
- [Contributions](#contributions)
    - [Reporting Bugs](#reporting-bugs)
    - [Suggesting Enhancements](#suggesting-enhancements)
    - [Pull Requests](#pull-requests)

## Branches management guidelines

* Default repository branch: `develop`. It contains all the latest changes, which are merged via the `Pull Requests`
* Any new features or bug fixes are implemented in the separate branches (`fn_`, `fix_`, etc.)
* Changes from the branches are merged into the `develop` after the `Pull Request` review
* If a certain state of the `develop` branch is considered as `feature-full` and `stable` - a release branch is created. It is immutable and keeps the code state for a specific point of time
* If a bug is uncovered in the `develop`, which also affects a certain release - the fix is cherry-picked into the `release` branch

```
            fix_branch1
                 \
t_now --->        \     |    |
                   \    * -- *  <--- "fix_branch1" merge into "develop" and cherry-pick into "release/2.0.0"
                    \  /|    |
                      / |    | 
                     /  |    |  \
                    /   |   /    \
                   *    |  /      release/2.0.0 branch
                    \   | /       
                     \  |/       <--- code is stable: release time!
                      \ |
                       \|
                        *        <--- "fn_branch2" merge into "develop"
                        |\
                        | \
                        |  \
                        *   \    <--- "fn_branch1" merge into "develop"
                       /|    *   <--- feature commit
                      / |    |
                     /  |    *   <--- feature commit
                    /   |   /
                   *    |  /     <--- feature commit
                    \   | / 
                     \  |/  \
                  /   \ |    \
                 /     \|     \
                /       |      fn_branch2
            fn_branch1  |
                        |
t_0 --->              develop
```
## CI process description

* When a commit is pushed to the `develop` branch, Travis CI receives a notification and runs a build automatically. 
* A Travis CI build process executes `./gradlew -PbuildNumber=${TRAVIS_BUILD_NUMBER}.${TRAVIS_COMMIT} build zip --no-daemon` command on the `script` main phase. 
* After a successful build, Travis CI uploads the build result `fonda*.zip` to Amazon S3 `s3://fonda-oss-builds/builds/develop/` bucket automatically.
* To get the `fonda*.zip` from S3 bucket you can follow the link `https://fonda-oss-builds.s3.amazonaws.com/builds/develop/fonda-${TRAVIS_BUILD_NUMBER}.${TRAVIS_COMMIT}.zip` 
in your browser or download file from the command console by `wget` or `curl` commands.
```
1. Commit to "develop" branch
                |                               
                |                                                
                * <--- "fn_branch1" merge into "develop"        
               /|          
              / |
             /  |
            *   |                     
           / \  |
          /   \ |             
         /     \|
fn_branch1      | 
                |
                |
             develop

2. Run build command on the Travis CI:

./gradlew -PbuildNumber=${TRAVIS_BUILD_NUMBER}.${TRAVIS_COMMIT} build zip --no-daemon

3. If the `step 2` success:
                                         _________
                                       // Amazon  \\
  _____________                       // ___S3____ \\                        _____________
 |\___________/|   file transfer     || _|_|_|_|_|_ ||   file transfer      |___Browser___|
 | fonda-*.zip | ----------------->  || _|_|_|_|_|_ ||  ----------------->  |             | 
 |_____________|       to S3         ||_|_|_|_|_|_|_||    to user           |_____________|
```

## Contributions

You can get started to contribute to the Fonda in several ways by creating:
- issue: [reporting bugs](#reporting-bugs) or [suggesting enhancements](#suggesting-enhancements)
- [pull requests](#pull-requests)

**Issues** are the way to track the problems, ideas, questions that would be raised while using the code in a repository.
These can include bugs to fix, features to add, or documentation that looks outdated. 
It can also be used to keep track of tasks, enhancements for the codebase.
The issue shall be discussed by repository maintainers and contributors first. If the issue is relevant and requires a solution, then it should be implemented and updated in the repository via pull request.

**Pull request** allows you made a feature implementation or a bug-fix in the code to a repository.  
Maintainers can see the changes and suggest modifications in the code. 
If the changes are acceptable, they can merge it to the branch requested.

### Reporting Bugs

This section guide is submitting a bug report for Fonda.

Bugs are tracked as GitHub [issues](https://github.com/epam/fonda/issues). Create an issue on the repository and provide the bug information by filling in the [template](https://github.com/epam/fonda/blob/develop/.github/ISSUE_TEMPLATE/bug_report.md).
This will keep your bugs tidy and relevant. 
When you are creating a bug report, please include as many details as possible.

> **Note**: Before creating bug reports, please check the bug is not already described in existing [issues](https://github.com/epam/fonda/issues).

### Suggesting Enhancements

This section guide is submitting an enhancement suggestion for Fonda.

Enhancement suggestions are tracked as GitHub [issues](https://github.com/epam/fonda/issues). 
Create an issue on the repository and provide the enhancement information by filling in the [template](https://github.com/epam/fonda/blob/develop/.github/ISSUE_TEMPLATE/feature_request.md).

### Pull Requests

This section guide is submitting an pull requests for Fonda.

Contributions to Fonda should be made in the form of GitHub pull [requests](https://github.com/epam/fonda/pulls).
Create an PR on the repository and describe the enhancement by filling in the [template](https://github.com/epam/fonda/blob/develop/.github/ISSUE_TEMPLATE/pull_request_template.md).
Each pull request will be reviewed by a core Fonda maintainers who has appropriate permissions.

Please follow these steps to have your contribution considered by the maintainers:

- Branch from the `develop` branch. If needed, rebase to the current `develop`
  branch before submitting your pull request. If it doesn't merge cleanly with
  master you may be asked to rebase your changes.

- Each PR should compile and pass tests.

- Each PR should pass `PMD`, `checkstyle`, `JaCoCo` checks.

- Add tests relevant to the fixed bug or new feature.  


>_All code in this repository is under the Apache License, Version 2.0._