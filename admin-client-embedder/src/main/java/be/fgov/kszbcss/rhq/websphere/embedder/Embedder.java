/*
 * RHQ WebSphere Plug-in
 * Copyright (C) 2012 Crossroads Bank for Social Security
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 2, as
 * published by the Free Software Foundation, and/or the GNU Lesser
 * General Public License, version 2.1, also as published by the Free
 * Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License and the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * and the GNU Lesser General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package be.fgov.kszbcss.rhq.websphere.embedder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;

public class Embedder {
    private static final Name MANIFEST_ATTRIBUTE_NAME = new Attributes.Name("Embedded-Admin-Client");
    private static final String MANIFEST_ATTRIBUTE_VALUE = "true";
    
    private static String loadVersion() throws IOException {
        Properties props = new Properties();
        InputStream in = Embedder.class.getResourceAsStream("embedder.properties");
        props.load(in);
        in.close();
        return props.getProperty("version");
    }
    
    private static void addLibrary(JarOutputStream out, File library) throws IOException {
        out.putNextEntry(new JarEntry("lib/" + library.getName()));
        InputStream in = new FileInputStream(library);
        try {
            IOUtils.copy(in, out);
        } finally {
            in.close();
        }
        out.closeEntry();
    }
    
    public static void main(String[] args) throws Exception {
        String version = loadVersion();
        
        System.out.println("Admin Client Embedder version " + version);
        
        String defaultInput = "rhq-websphere-agent-plugin-" + version + ".jar";
        String defaultOutput = "rhq-websphere-agent-plugin-with-adminclient-" + version + ".jar";
        
        Options options = new Options();
        
        {
            Option option = new Option("i", true, "the path to the original agent plug-in JAR; defaults to " + defaultInput);
            option.setLongOpt("input");
            option.setArgName("file");
            options.addOption(option);
        }
        
        {
            Option option = new Option("o", true, "the path for the output file (i.e. the plug-in JAR with the embedded admin client libraries); defaults to " + defaultOutput);
            option.setLongOpt("output");
            option.setArgName("file");
            options.addOption(option);
        }
        
        {
            Option option = new Option("w", true, "the WebSphere Application Server home directory");
            option.setLongOpt("washome");
            option.setArgName("dir");
            option.setRequired(true);
            options.addOption(option);
        }
        
        if (args.length == 0) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(100);
            formatter.printHelp(Embedder.class.getName(), options, true);
            return;
        }
        
        CommandLineParser parser = new GnuParser();
        CommandLine cmdLine;
        try {
            cmdLine = parser.parse(options, args);
        } catch (ParseException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
            return; // Make compiler happy
        }
        
        File wasHome = new File(cmdLine.getOptionValue("w"));
        
        JarInputStream in = new JarInputStream(new FileInputStream(cmdLine.getOptionValue("i", defaultInput)));
        JarOutputStream out = new JarOutputStream(new FileOutputStream(cmdLine.getOptionValue("o", defaultOutput)));
        Manifest manifest = in.getManifest();
        Attributes attrs = manifest.getMainAttributes();
        if (!version.equals(attrs.getValue("Implementation-Version"))) {
            System.err.println("Unexpected plug-in version");
            System.exit(2);
        }
        if (MANIFEST_ATTRIBUTE_VALUE.equals(attrs.getValue(MANIFEST_ATTRIBUTE_NAME))) {
            System.err.println("The plug-in already contains the admin client libraries");
            System.exit(2);
        }
        attrs.put(MANIFEST_ATTRIBUTE_NAME, MANIFEST_ATTRIBUTE_VALUE);
        out.putNextEntry(new JarEntry("META-INF/MANIFEST.MF"));
        manifest.write(out);
        out.closeEntry();
        JarEntry entry;
        while ((entry = in.getNextJarEntry()) != null) {
            out.putNextEntry(entry);
            IOUtils.copy(in, out);
            out.closeEntry();
        }
        addLibrary(out, new File(wasHome, "runtimes/com.ibm.ws.admin.client_7.0.0.jar"));
        addLibrary(out, new File(wasHome, "plugins/com.ibm.ws.security.crypto.jar"));
        in.close();
        out.close();
    }
}
