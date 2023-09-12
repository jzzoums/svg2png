package com.svgtopng.controller;


import com.svgtopng.entity.SvgAttrObj;
import com.svgtopng.utility.MinIoUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

@Controller
@Slf4j
public class SvgToPngController {
    @Autowired
    private MinIoUtil minIoUtil;
    @Value("${svg.labels}")
    private String svgLabels;

    @GetMapping(path = "download", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public void download(float height, float width, MultipartFile file, HttpServletResponse response) throws IOException, TranscoderException {
        //MultipartFile转InputStream
        InputStream in = new ByteArrayInputStream(file.getBytes());
        Transcoder transcoder = new PNGTranscoder();
        //设置png图片的宽和长
        transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, width);
        transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, height);
        try {
            TranscoderInput input = new TranscoderInput(in);
            //清空response
            response.reset();
            //强制下载不打开
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            //设置编码为UTF_8
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            //Content-Disposition的作用：告知浏览器以何种方式显示响应返回的文件，用浏览器打开还是以附件的形式下载到本地保存
            //attachment表示以附件方式下载 inline表示在线打开 "Content-Disposition:inline; filename=文件名.mp3"
            //filename表示文件的默认名称，因为网络传输只支持URL编码，因此需要将文件名URL编码后进行传输，前端收到后需要反编码才能获取到真正的名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode((Objects.requireNonNull(file.getOriginalFilename()).split("\\."))[0], StandardCharsets.UTF_8.name()) + ".png");
            TranscoderOutput output = new TranscoderOutput(response.getOutputStream());
            transcoder.transcode(input, output);
        } finally {
            in.close();
        }
    }
    public void generatePng(String original, String des) throws Exception {
//        String src = "D:\\device.svg";
        File desc = new File(des);
        if (!desc.exists()) {
            desc.createNewFile();
        }
        FileInputStream fis = new FileInputStream(original);
        OutputStream out = new FileOutputStream(desc);
        out = new BufferedOutputStream(out);
        Transcoder transcoder = new PNGTranscoder();
        try {
            TranscoderInput input = new TranscoderInput(fis);
            try {
                TranscoderOutput output = new TranscoderOutput(out);
                transcoder.transcode(input, output);
            } finally {
                out.close();
            }
        } finally {
            fis.close();
        }
    }
    @GetMapping("/index")
    public String index() {
        return "index";
    }
    @GetMapping("/test")
    public String test() {
        return "test";
    }
    @PostMapping("/uploadFile")
    @ResponseBody
    public Map uploadFile(@RequestParam("file") MultipartFile file, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        String dir = System.getProperty("user.home");
        String originalSvgPath = dir + "/" + file.getOriginalFilename();
        File originalSvg = new File(originalSvgPath);
//        file.transferTo(originalSvg);
        FileUtils.copyInputStreamToFile(file.getInputStream(), originalSvg);

        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
        Document document = factory.createDocument(new File(originalSvgPath).toURI().toString());
        Map<String, List<String>> resultMap = new HashMap<>();
        List<String> labels = Arrays.asList(svgLabels.split(","));
        for (String label : labels) {
            resultMap.put(label, new ArrayList<>());
        }
        Element svgEle = document.getDocumentElement();
        result.put("svgString", transformationSVG(svgEle));
        NamedNodeMap attributeList = svgEle.getAttributes();
        for (int j = 0; j < attributeList.getLength(); j++) {
            String attrName = attributeList.item(j).getNodeName();
            if ("width".equals(attrName) || "height".equals(attrName)) {
                result.put(attrName, attributeList.item(j).getNodeValue());
            }
        }
        NodeList nodeList = document.getDocumentElement().getChildNodes();
        traversalNodes(nodeList, resultMap, labels);
        List<SvgAttrObj> attrAndValues = new ArrayList<>();
        for (String label : labels) {
            List<String> values = resultMap.get(label);
            log.info(label + " = " + values);
            for (String val : values) {
                attrAndValues.add(new SvgAttrObj(label, val));
            }
        }
//        String[] orgPaths = originalSvgPath.split("\\.");
//        String desPngPath = orgPaths[0] + ".png";
//        System.out.println("descPath = " + desPngPath);
//        generatePng(originalSvgPath, desPngPath);

//        try {
//            if (!(file.getOriginalFilename() == null || "".equals(file.getOriginalFilename()))) {
//                Map<String, String> res = minIoUtil.upload(file);
//                if (res.isEmpty()) {
//                    result.put("msg", "文件上传异常!");
//                    result.put("result", null);
//                    return result;
//                }
//                String[] nameArray = file.getOriginalFilename().split("\\.");
//                String suffix = nameArray[nameArray.length - 1];
//                result.put("msg", "文件上传成功");
//                result.put("savedFileName", res.get("filename"));
//                result.put("path", res.get("path"));
//                result.put("fileName", file.getOriginalFilename());
//                result.put("fileSize", file.getSize());
//                result.put("fileExt", suffix);
//            }
//        } catch (Exception e) {
//            result.put("msg", "上传失败！");
//            result.put("path", null);
//            e.printStackTrace();
//        }
        result.put("attrAndValues", attrAndValues);
        originalSvg.delete();
        return result;
    }
    private void traversalNodes(NodeList nodeList, Map<String, List<String>> map, List<String> labels) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                NamedNodeMap attributeList = element.getAttributes();
                for (int j = 0; j < attributeList.getLength(); j++) {
                    String attrName = attributeList.item(j).getNodeName();
                    if (labels.contains(attrName)) {
                        List<String> list = map.get(attrName);
                        list.add(attributeList.item(j).getNodeValue());
                    }
                }
                NodeList childrenList = node.getChildNodes();
                if (childrenList.getLength() > 0) {
                    traversalNodes(childrenList, map, labels);
                }
            }
        }
    }

    private String transformationSVG(Element element) {
        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = transFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        }
        StringWriter buffer = new StringWriter();
        assert transformer != null;
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        try {
            transformer.transform(new DOMSource(element), new StreamResult(buffer));
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }
}
