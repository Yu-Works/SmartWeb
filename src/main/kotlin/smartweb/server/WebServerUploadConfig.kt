package smartweb.server

data class WebServerUploadConfig(
    val maxSize: String = "10m",
    val maxFile: Int = -1,
    val singleFileMaxSize: String = "10m",
    val writeTmpFileSize: String = "10k",
    val tempDir: String = System.getProperty("java.io.tmpdir"),
)