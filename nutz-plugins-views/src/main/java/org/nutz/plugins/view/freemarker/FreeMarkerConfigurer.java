package org.nutz.plugins.view.freemarker;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.Mvcs;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;

public class FreeMarkerConfigurer {

	private final static Log log = Logs.get();
	private Configuration configuration;
	private String prefix;
	private String suffix;
	private FreemarkerDirectiveFactory freemarkerDirectiveFactory;
	private Map<String, Object> tags = new HashMap<String, Object>();
	private final StringBuilder pro = new StringBuilder();

	/**
	 * 默认配置 前缀 /WEB-INF 也就是模板基路径 后缀 .ftl
	 * 
	 */
	public FreeMarkerConfigurer() {
		Configuration configuration = new Configuration();
		this.initp(configuration, Mvcs.getServletContext(), "WEB-INF", ".ftl", null);
	}

	public FreeMarkerConfigurer(Configuration configuration, ServletContext sc, String prefix, String suffix, FreemarkerDirectiveFactory freemarkerDirectiveFactory) {
		this.initp(configuration, sc, prefix, suffix, freemarkerDirectiveFactory);
	}

	protected void initp(Configuration configuration, ServletContext sc, String prefix, String suffix, FreemarkerDirectiveFactory freemarkerDirectiveFactory) {
		this.configuration = configuration;
		this.prefix = sc.getRealPath(prefix);
		this.suffix = suffix;
		this.freemarkerDirectiveFactory = freemarkerDirectiveFactory;
		if (this.prefix == null)
			this.prefix = sc.getRealPath("/") + prefix;
		pro.append("tag_syntax=auto_detect");
		pro.append("template_update_delay=-1");
		pro.append("defaultEncoding=UTF-8");
		pro.append("url_escaping_charset=UTF-8");
		pro.append("locale=zh_CN");
		pro.append("boolean_format=true,false");
		pro.append("datetime_format=yyyy-MM-dd HH:mm:ss");
		pro.append("date_format=yyyy-MM-dd");
		pro.append("time_format=HH:mm:ss");
		pro.append("number_format=0.######");
		pro.append("whitespace_stripping=true");
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public void init() {
		try {
			initFreeMarkerConfigurer();
			Iterator<Entry<String, Object>> iterator = tags.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, Object> entry = iterator.next();
				configuration.setSharedVariable(entry.getKey(), entry.getValue());
			}
			if (freemarkerDirectiveFactory == null)
				return;
			for (FreemarkerDirective freemarkerDirective : freemarkerDirectiveFactory.getList()) {
				configuration.setSharedVariable(freemarkerDirective.getName(), freemarkerDirective.getTemplateDirectiveModel());
			}
		} catch (IOException e) {
			log.error(e);
		} catch (TemplateException e) {
			log.error(e);
		}
	}

	public String getSuffix() {
		return suffix;
	}

	public String getPrefix() {
		return prefix;
	}

	protected void initFreeMarkerConfigurer() throws IOException, TemplateException {
		Properties p = new Properties();
		String path = freemarkerDirectiveFactory.getFreemarker();
		File file = Files.findFile(path);
		if (Lang.isEmpty(file)) {
			p.load(Streams.wrap(pro.toString().getBytes()));
		} else {
			p.load(Streams.fileIn(file));
		}
		configuration.setSettings(p);
		File f = Files.findFile(prefix);
		configuration.setDirectoryForTemplateLoading(f);
	}

	public void setTags(Map<String, Object> map) {
		Iterator<Entry<String, Object>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, Object> entry = iterator.next();
			String key = entry.getKey();
			Object obj = entry.getValue();
			tags.put(key, obj);
		}
	}
}