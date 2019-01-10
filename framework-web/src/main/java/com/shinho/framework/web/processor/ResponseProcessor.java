package com.shinho.framework.web.processor;


import com.shinho.framework.web.ResponseType;

public interface ResponseProcessor{

	Object processor(Object obj, ResponseType type);
	
	Object processorForException(Exception e, ResponseType type) throws Exception;

}
