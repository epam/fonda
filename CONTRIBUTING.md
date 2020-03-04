# Branches management guidelines

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
