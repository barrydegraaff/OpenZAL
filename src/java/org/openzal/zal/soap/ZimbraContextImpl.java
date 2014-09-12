/*
 * ZAL - The abstraction layer for Zimbra.
 * Copyright (C) 2014 ZeXtras S.r.l.
 *
 * This file is part of ZAL.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZAL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.openzal.zal.soap;

import org.jetbrains.annotations.Nullable;
import org.openzal.zal.Continuation;
import org.openzal.zal.Jetty;
import org.openzal.zal.Utils;
import com.zimbra.common.soap.Element;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.SoapServlet;
import com.zimbra.soap.ZimbraSoapContext;
import org.openzal.zal.log.ZimbraLog;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class ZimbraContextImpl implements ZimbraContext
{
  public Element getRequest()
  {
    return mRequest;
  }

  public Map<String, Object> getContext()
  {
    return mContext;
  }

  @Nullable private final Element             mRequest;
  private final           Map<String, Object> mContext;
  private final           ZimbraSoapContext   mZimbraSoapContext;
  private final           Map<String, String> mMap;

  public ZimbraSoapContext getZimbraSoapContext()
  {
    return mZimbraSoapContext;
  }

  ZimbraContextImpl(Map<String, Object> context)
  {
    mContext = context;
    mZimbraSoapContext = (ZimbraSoapContext) context.get(SoapEngine.ZIMBRA_CONTEXT);
    mMap = Collections.emptyMap();
    mRequest = null;
  }

  ZimbraContextImpl(Element request, Map<String, Object> context)
  {
    mRequest = request;
    mContext = context;
    mZimbraSoapContext = (ZimbraSoapContext) context.get(SoapEngine.ZIMBRA_CONTEXT);
    mMap = new HashMap<String, String>(32);

    Set<Element.Attribute> attributes = request.listAttributes();
    for (Element.Attribute attribute : attributes)
    {
      String key = attribute.getKey();
      String value = attribute.getValue();
      if (value.isEmpty())
      {
        value = null;
      }
      mMap.put(key, value);
    }

    for (Element element : request.listElements())
    {
      String key = element.getName();
      String value = element.getText();
      if (value.isEmpty())
      {
        value = null;
      }
      mMap.put(key, value);
    }
  }

  class StubSoapNode implements SoapNode
  {
    @Override
    public SoapNode getSubNode(String name)
    {
      return new StubSoapNode();
    }

    @Override
    public Map<String, String> getParameterMap()
    {
      return Collections.emptyMap();
    }

    @Override
    public String getParameter(String key, String def)
    {
      return def;
    }

    @Override
    public String getNodeName()
    {
      return "";
    }

    @Override
    public String getText()
    {
      return "";
    }
  }

  @Override
  public SoapNode getSubNode(String name)
  {
    Element subElement;
    try
    {
      subElement = mRequest.getElement(name);
    }
    catch (com.zimbra.common.service.ServiceException e)
    {
      return new StubSoapNode();
    }

    return new ZimbraContextImpl(subElement,mContext);
  }

  @Override
  public Map<String, String> getParameterMap()
  {
    return mMap;
  }

  @Override
  public String getParameter(String key, String def)
  {
    String value = mMap.get(key);
    return (value == null) ? def : value;
  }

  @Override
  public String getNodeName()
  {
    return mRequest.getName();
  }

  @Override
  public String getText()
  {
    return mRequest.getText();
  }

  @Override
  public String getTargetAccountId()
  {
    return mZimbraSoapContext.getRequestedAccountId();
  }

  @Override
  public String getAuthenticatedAccontId()
  {
    return mZimbraSoapContext.getAuthtokenAccountId();
  }

  @Override
  public String getRequesterIp()
  {
    Object ip = mContext.get("request.ip");
    return (ip != null) ? ip.toString() : "";
  }

  private static Method sDispatchRequest = null;

  /* Element dispatchRequest(Element request, Map<String, Object> context, ZimbraSoapContext zsc); */
  static
  {
    try
    {
      Class partypes[] = {
        Element.class,
        Map.class,
        ZimbraSoapContext.class
      };

      sDispatchRequest = SoapEngine.class.getDeclaredMethod("dispatchRequest", partypes);
      sDispatchRequest.setAccessible(true);
    }
    catch (Throwable ex)
    {
      ZimbraLog.extensions.fatal("Reflection Initialization Exception: " + Utils.exceptionToString(ex));
    }
  }

  @Override
  public SoapResponse execLocalRequest()
  {
    SoapEngine soapEngine = (SoapEngine) mContext.get(SoapEngine.ZIMBRA_ENGINE);
    ZimbraSoapContext zimbraSoapContext = (ZimbraSoapContext) mContext.get(SoapEngine.ZIMBRA_CONTEXT);
    try
    {
      Element response = (Element)sDispatchRequest.invoke(soapEngine, mRequest, mContext, mZimbraSoapContext );
      return new SoapResponseImpl(response, new InternalDocumentHelper.ElementFactory(zimbraSoapContext));
    }
    catch (IllegalAccessException e)
    {
      throw new RuntimeException(e);
    }
    catch (InvocationTargetException e)
    {
      throw new RuntimeException(e.getCause());
    }
  }

  @Override
  public HttpServletRequest getHttpServletRequest()
  {
    return (HttpServletRequest) mContext.get(SoapServlet.SERVLET_REQUEST);
  }

  @Override
  public Continuation getContinuation()
  {
    return Jetty.getContinuation(getHttpServletRequest());
  }

  @Override
  public boolean isDelegatedAuth()
  {
    return mZimbraSoapContext.isDelegatedRequest();
  }
}
