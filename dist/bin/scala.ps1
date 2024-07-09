# Environment setup
$global:_EXITCODE = 0

$scriptPath = $PSScriptRoot
$global:_PROG_HOME = $scriptPath.TrimEnd('\')

. "$_PROG_HOME\bin\common.bat"
if ($LASTEXITCODE -ne 0) { goto end }

# Main
setScalaOpts

. "$_PROG_HOME\bin\cli-common-platform.ps1"

# SCALA_CLI_CMD_WIN is an array, set in cli-common-platform.bat.
# WE NEED TO PASS '--skip-cli-updates' for JVM launchers but we actually don't need it for native launchers
& $env:SCALA_CLI_CMD_WIN "--prog-name" "scala" "--skip-cli-updates" "--cli-default-scala-version" "$env:_SCALA_VERSION" "-r" "$env:MVN_REPOSITORY" $args

if ($LASTEXITCODE -ne 0) {
    $global:_EXITCODE = 1
    goto end
}

goto end

# Subroutines

function setScalaOpts {
    # Find the index of the first colon in _PROG_HOME
    $index = $global:_PROG_HOME.IndexOf(':')
    if ($index -eq -1) { $index = 0 }

    $global:_SCALA_VERSION = ""
    $global:MVN_REPOSITORY = "file:///$($global:_PROG_HOME -replace '\\', '/')/maven2"

    # Read version from VERSION_FILE
    $versionFilePath = "$global:_PROG_HOME\VERSION"
    if (Test-Path $versionFilePath) {
        $lines = Get-Content $versionFilePath
        foreach ($line in $lines) {
            if ($line.StartsWith("version:=")) {
                $global:_SCALA_VERSION = $line.Substring(9)
                break
            }
        }
    }
}

# Cleanups
end
exit $global:_EXITCODE
