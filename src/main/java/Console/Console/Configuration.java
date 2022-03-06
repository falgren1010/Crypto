package Console.Console;

public enum Configuration {
    instance;

    public final String fileSeperator = System.getProperty("file.seperator");
    public final String userDirectory = System.getProperty("user.dir");

    public final String pathToJavaArchive = userDirectory + fileSeperator + "java" + fileSeperator + "Console" + fileSeperator+ "Report" + fileSeperator + "jar";
}
