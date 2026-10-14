import java.io.*;
import java.nio.file.*;
import java.util.zip.GZIPOutputStream;
class MakeSmokeTemplate {
    static void named(DataOutputStream d,int type,String name)throws IOException{d.writeByte(type);d.writeUTF(name);}
    static void pos(DataOutputStream d,String name,int x,int y,int z)throws IOException{
        named(d,9,name);d.writeByte(3);d.writeInt(3);d.writeInt(x);d.writeInt(y);d.writeInt(z);
    }
    public static void main(String[]args)throws Exception{
        Path out=Path.of("src/smoke/resources/data/friendmonsters_smoke/structure/arena.nbt");Files.createDirectories(out.getParent());
        try(var d=new DataOutputStream(new GZIPOutputStream(Files.newOutputStream(out)))){
            named(d,10,"");named(d,3,"DataVersion");d.writeInt(4671);pos(d,"size",16,8,16);
            named(d,9,"palette");d.writeByte(10);d.writeInt(1);named(d,8,"Name");d.writeUTF("minecraft:stone");d.writeByte(0);
            named(d,9,"blocks");d.writeByte(10);d.writeInt(256);
            for(int x=0;x<16;x++)for(int z=0;z<16;z++){pos(d,"pos",x,0,z);named(d,3,"state");d.writeInt(0);d.writeByte(0);}
            named(d,9,"entities");d.writeByte(10);d.writeInt(0);d.writeByte(0);
        }
    }
}
