import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.UUID;
import java.io.*;
import java.util.concurrent.TimeUnit;

class ExportGLB {

    private static final String kicad_img_id = "a37c2763212f";

    private static final String kicad_img_home_path = "/home/kicad";

    public static String export_glb(String root_sch_file_name) {

        Process process_export = null;
        String kicad_project_dir = new File(root_sch_file_name).getParent();
        String pcb_name = new File(root_sch_file_name).getName();
        String mounted_prj_path = kicad_img_home_path + "/" + UUID.randomUUID().toString();
        String mouted_pcb_fp = mounted_prj_path + "/" + pcb_name;
        String output_file_name = UUID.randomUUID().toString() + ".glb";
        String docker_output_fn = mounted_prj_path + "/" + output_file_name;

        String[] firstCmd = { "docker", "run",
                "-v", kicad_project_dir + ":" + mounted_prj_path,
                kicad_img_id, "kicad-cli", "pcb",
                "export", "glb", " --subst-models",
                mouted_pcb_fp, "-o",
                docker_output_fn
        };

        try {
            process_export = Runtime.getRuntime().exec(firstCmd);
        } catch (IOException ex) {
            Logger.getLogger(ExportGLB.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(String.join(" ", firstCmd));

        try {
            process_export.waitFor();
        } catch (InterruptedException ex) {
            Logger.getLogger(ExportGLB.class.getName()).log(Level.SEVERE, null, ex);
        }

        String c_output_file_name = UUID.randomUUID().toString() + ".glb";
        String c_docker_output_fn = mounted_prj_path + "/" + c_output_file_name;

        String[] secondCMD = { "docker", "run", "-v", kicad_project_dir + ":" + mounted_prj_path,
                kicad_img_id, "npx", "gltfpack ",
                "-i",
                docker_output_fn, " -v -cc -tc -ts 0.5 ", "-o",
                c_docker_output_fn
        };

        System.out.println(String.join(" ", secondCMD));

        Process process_pack = null;

        try {
            process_pack = Runtime.getRuntime().exec(secondCMD);
        } catch (IOException ex) {
            Logger.getLogger(ExportGLB.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            process_pack.waitFor();
        } catch (InterruptedException ex) {
            Logger.getLogger(ExportGLB.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return Paths.get(kicad_project_dir + "/" + c_output_file_name).toString();
    }

    public static void main(String[] args) {
        String unziped_prj_path = "/home/hq/kicad/complex_hierarchy"; // NOETE 项目文件夹需要有写入权限
        String pcb_fn = "complex_hierarchy.kicad_pcb";
        String net_list = export_glb(unziped_prj_path + "/" + pcb_fn);
        System.out.println(net_list);

    }
}
