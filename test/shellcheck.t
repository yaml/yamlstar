#!/usr/bin/env bash

source test/init

+cmd:ok-ver shellcheck 0.11.0 ||
  plan skip-all "Test requires shellcheck 0.11.0 to be installed"

# 1090 - Can't follow dynamic source path
# 1091 - Can't open sourced file
# 2030 - Variable modification local to subshell
# 2031 - Variable modified in subshell may be lost
# 2034 - Variable appears unused
# 2086 - Expansion intentionally feeds shell words in legacy release script
# 2154 - Variable referenced but not assigned
# 2155 - Declaration and assignment combined in legacy release script
# 2162 - read without -r in interactive legacy prompt
# 2207 - Array via unquoted command substitution
# 2206 - Unquoted array initialization in benchmark helper scripts
# 2231 - Unquoted glob in legacy release packaging loop
# 2269 - Intentional self-assignment in environment wrapper script
skip=1090,1091,2030,2031,2034,2086,2154,2155,2162,2206,2207,2231,2269

while read -r file; do
  [[ -h $file ]] && continue
  [[ -f $file ]] || continue

  shebang=$(head -n1 "$file" | LC_ALL=C tr -d '\0')

  if [[ $file == *.bash ]] ||
     [[ $shebang == '#!'*[/\ ]bash ]]
  then
    ok "$(shellcheck -x -e "$skip" "$file")" \
      "Bash file '$file' passes shellcheck"

  elif
    [[ $file == *.sh ]] ||
    [[ $shebang == '#!'*[/\ ]sh ]]
  then
    ok "$(shellcheck -x -e "$skip" "$file")" \
      "Shell script file '$file' passes shellcheck"
  fi
done < <(
  git ls-files
)

done-testing
