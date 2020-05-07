#Contributing Guidelines
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