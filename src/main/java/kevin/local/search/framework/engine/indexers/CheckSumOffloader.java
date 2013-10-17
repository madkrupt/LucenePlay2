package kevin.local.search.framework.engine.indexers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 12/8/12
 * Time: 2:26 PM
 */
class CheckSumOffloader implements Callable<String> {
        private static final String ALGORITHM = "SHA-1";
        private static final int BUFFER_SIZE = 1024 * 64; // 64KB Buffer

        private final Path file;

        public CheckSumOffloader(Path file) {
            this.file = file;
        }

        public String call() throws IOException, NoSuchAlgorithmException {
            try (FileChannel fileChannel = FileChannel.open(file, StandardOpenOption.READ)) {

//                BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
//                            if (attr.isDirectory() || (!attr.isRegularFile())) throw new RuntimeException("File is strange: " + file.toString());
//                            ret.setFileSize( attr.size() );
//                            ret.setCreationTime( attr.creationTime() );
//                            ret.setLastAccessTime( attr.lastAccessTime() );
//                            ret.setLastModifyTime( attr.lastModifiedTime() );
//
                /*****************************************************************************************/

                final ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                final MessageDigest md = MessageDigest.getInstance(ALGORITHM);

                while (true) {
                    int bytesRead = fileChannel.read(byteBuffer);
                    if (bytesRead > 0) {
                        byteBuffer.flip();
                        md.update(byteBuffer);
                        byteBuffer.clear();

                    }
                    else {
                        break;
                    }
                }
                //done
                String ret = byteArrayToHexString(md.digest());

                //cleanup
                byteBuffer.clear();
                md.reset();

                return ret;
            }
        }
    private static String byteArrayToHexString(byte[] b) {
        StringBuffer sb = new StringBuffer(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            int v = b[i] & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString();
    }
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        String result = new CheckSumOffloader(Paths.get("/Users/madkrupt/Desktop/tstmusic/1/Appleseed/01 Aesop Rock - Appleseed Intro.mp3")).call();
        System.out.printf("%s%n", result);
    }
}
