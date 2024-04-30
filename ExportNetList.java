import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.UUID;

class ExportNetLIst {

    private static final String kicad_img_id = "ghcr.io/liangtie/kicad:lite";

    private static final String kicad_img_home_path = "/home/kicad";

    public static String export_allegro(String root_sch_file_name) {

        Process process = null;
        String kicad_project_dir = new File(root_sch_file_name).getParent();
        String root_sch_name = new File(root_sch_file_name).getName();
        String output_file_name = UUID.randomUUID().toString();
        String mounted_prj_path = kicad_img_home_path + "/" + UUID.randomUUID().toString();
        String mouted_sch_root_path = mounted_prj_path + "/" + root_sch_name;

        String[] firstCmd = { "docker", "run", "--rm", "-v", kicad_project_dir + ":" + mounted_prj_path,
                kicad_img_id, "kicad-cli", "sch",
                "export", "netlist", "--format", "kicadxml",
                mouted_sch_root_path, "-o",
                mounted_prj_path + "/" + output_file_name
        };

        try {
            process = Runtime.getRuntime().exec(firstCmd);
        } catch (IOException ex) {
            Logger.getLogger(ExportNetLIst.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            process.waitFor();
        } catch (InterruptedException ex) {
            Logger.getLogger(ExportNetLIst.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            return new String(Files.readAllBytes(Paths.get(kicad_project_dir + "/" + output_file_name)),
                    StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Logger.getLogger(ExportNetLIst.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "";
    }

    public static void main(String[] args) {
        String unziped_prj_path = "/home/hq/kicad/psprice"; // NOETE 项目文件夹需要有写入权限
        String root_sch_fn = "pspice.kicad_sch";
        String net_list = export_allegro(unziped_prj_path + "/" + root_sch_fn);
        System.out.print(net_list);

    }
}
