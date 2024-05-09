import com.google.cloud.storage.Storage.BlobWriteOption;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.transfermanager.ParallelUploadConfig;
import com.google.cloud.storage.transfermanager.TransferManagerConfig;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

class X {
  private static final String BUCKET_NAME = "SOME BUCKET";
  private static final String PREFIX = "prefix";

  /** Will create a large local file at this path. */
  private static final String TMP_PATH = "/tmp/x";

  public static void main(String[] args) throws Exception {
    var path = Path.of(TMP_PATH);
    try (var ch = Files.newByteChannel(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
      ch.position(4L * 1024L * 1024L * 1024L - 1);
      ch.write(ByteBuffer.wrap(new byte[1]));
    }
    var options = StorageOptions.newBuilder().build();
    var tmc =
        TransferManagerConfig.newBuilder()
            .setStorageOptions(options)
            .setAllowParallelCompositeUpload(true)
            .build();
    try (var tm = tmc.getService()) {
      System.out.println(
          tm.uploadFiles(
                  List.of(path),
                  ParallelUploadConfig.newBuilder()
                      .setBucketName(BUCKET_NAME)
                      .setPrefix(PREFIX)
                      .setWriteOptsPerRequest(List.of(BlobWriteOption.disableGzipContent()))
                      .build())
              .getUploadResults());
    }
  }
}
