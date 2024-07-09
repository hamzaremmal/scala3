# We need to escape % in the java command path, for some reason this doesn't work in common.bat
$global:_JAVACMD = $global:_JAVACMD -replace '%', '%%'

$global:SCALA_CLI_CMD_WIN = "`"$global:_JAVACMD`" -jar `"$global:_PROG_HOME\bin\scala-cli.jar`""
