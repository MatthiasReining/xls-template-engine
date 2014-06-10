/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sourcecoding.xte;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import jxl.write.WriteException;

/**
 *
 * @author Matthias
 */
public class XTEngine {

    OutputStream os;
    Map<String, Object> payloadMap = new HashMap<>();
    Object payloadObject = null;

    URL templateUrl;
    String encoding = "Cp1252"; //default

    public XTEngine setOutputStream(OutputStream os) {
        this.os = os;
        return this;
    }

    public XTEngine setPayload(Map<String, Object> payload) {
        if (payloadObject != null)
            throw new XlsTemplateEngineException("It's not possible to define a payload POJO and in parallel a 'normal' Map based payload!");

        //make a copy to protect for modifications / adds on root level
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            this.payloadMap.put(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public XTEngine addPayload(String path, Object payload) {
        if (payloadObject != null)
            throw new XlsTemplateEngineException("It's not possible to define a payload POJO and in parallel a 'normal' Map based payload!");
        this.payloadMap.put(path, payload);
        return this;
    }

    public XTEngine setPayloadPOJO(Object payload) {
        if (!payloadMap.isEmpty())
            throw new XlsTemplateEngineException("It's not possible to define a payload POJO and in parallel a 'normal' Map based payload!");
        this.payloadObject = payload;
        return this;
    }

    public XTEngine setTemplateUrl(URL templateUrl) {
        this.templateUrl = templateUrl;
        return this;
    }

    public XTEngine setXlsEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public void render() {
        if (templateUrl == null)
            throw new RuntimeException("Parameter 'templateUrl' has to be defined!");
        if (payloadMap == null)
            throw new RuntimeException("Parameter 'payload' has to be defined!");
        if (os == null)
            throw new RuntimeException("Parameter 'os' has to be defined!");

        try {

            WorkbookSettings settings = new WorkbookSettings();
            settings.setEncoding(encoding);

            Workbook template = Workbook.getWorkbook(templateUrl.openStream(), settings);

            Object payload = (payloadObject == null) ? payloadMap : payloadObject;
            new Renderer().run(payload, template, os);

        } catch (IOException | WriteException | BiffException ex) {
            throw new RuntimeException(ex);
        }
    }
}
