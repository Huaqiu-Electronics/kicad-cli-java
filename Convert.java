import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class Convert {

    private static final String kicadImgId = "ghcr.io/liangtie/kicad:lite";
    private static final String kicadImgHomePath = "/home/kicad";

    private static final String[] supportedExtensions = { "PcbDoc", "SchDoc" };
    private static final String[] conversionFormats = { "kicad_pcb", "kicad_sch" };

    private static final String[] TP_MAP = { "pcb", "sch" };

    public static void main(String[] args) {
        convert("D:/code/kicad/build/out/ad/PWRMOD-001-RevA.SchDoc");
        convert("D:/code/kicad/build/out/ad/PWRMOD-001-RevA.PcbDoc");
    }

    public static void convert(String oriFilePath) {
        File kicadProjectDir = new File(oriFilePath).getParentFile();
        String oriFileName = new File(oriFilePath).getName();
        String oriExtension = oriFileName.substring(oriFileName.lastIndexOf('.') + 1);

        // Check if file extension is supported
        int index = -1;
        for (int i = 0; i < supportedExtensions.length; i++) {
            if (supportedExtensions[i].equals(oriExtension)) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            System.out.println("Unsupported file extension: " + oriExtension);
            return;
        }

        String convertedFileName = oriFileName.replace(oriExtension, conversionFormats[index]);
        String kicadType = TP_MAP[index];

        String mountedPrjPath = kicadImgHomePath + "/" + UUID.randomUUID().toString().replace("-", "/");
        String mountedFilePath = mountedPrjPath + "/" + oriFileName.replace("\\", "/");
        String dockerOutputFilePath = mountedPrjPath + "/" + convertedFileName.replace("\\", "/");

        String[] firstCmd = { "docker", "run", "--rm",
                "-v", kicadProjectDir.getAbsolutePath().replace("\\", "/") + ":" + mountedPrjPath,
                kicadImgId, "kicad-cli", kicadType,
                "convert", "--output", dockerOutputFilePath,
                mountedFilePath };

        StringBuilder commandStringBuilder = new StringBuilder();
        for (String part : firstCmd) {
            commandStringBuilder.append(part).append(" ");
        }
        String commandString = commandStringBuilder.toString().trim();
        System.out.println(commandString);

        ProcessBuilder processBuilder = new ProcessBuilder(firstCmd);
        try {
            Process process = processBuilder.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
