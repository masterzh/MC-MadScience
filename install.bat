@ECHO OFF
echo  ================================
echo  CONFIGURES ECLIPSE PROJECT FILES
echo  ================================
pause
gradlew --refresh-dependencies setupDevWorkspace eclipse
pause