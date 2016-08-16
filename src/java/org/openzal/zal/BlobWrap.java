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

package org.openzal.zal;

import com.zimbra.cs.store.file.VolumeBlobProxy;
import com.zimbra.cs.store.file.VolumeStagedBlob;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openzal.zal.log.ZimbraLog;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class BlobWrap implements Blob
{
  @NotNull private final com.zimbra.cs.store.Blob mBlob;
  private final String mVolumeId;

  @NotNull
  public Object getWrappedObject()
  {
    return mBlob;
  }

  public BlobWrap(
    @NotNull Object blob,
    String volumeId
  )
  {
    if (!com.zimbra.cs.store.Blob.class.isAssignableFrom(blob.getClass()) || com.zimbra.cs.store.file.VolumeBlobProxy.class.isAssignableFrom(blob.getClass()))
    {
      throw new RuntimeException("Cannot handle blob of type " + blob.getClass());
    }
    if (blob == null)
    {
      throw new NullPointerException();
    }
    if (VolumeBlobProxy.isVolumeBlob(blob))
    {
      volumeId = String.valueOf(new VolumeBlobProxy(blob).getVolumeId());
    }
    if (volumeId == null && blob instanceof VolumeStagedBlob)
    {
      /* $if ZimbraVersion >= 7.0.0 $ */
      volumeId = String.valueOf(((VolumeStagedBlob) blob).getLocator());
      /* $else $
      volumeId = String.valueOf(((VolumeStagedBlob) blob).getStagedLocator());
      /* $endif $ */
    }
    mVolumeId = volumeId;
    mBlob = (com.zimbra.cs.store.Blob) blob;
  }

  public File getFile()
  {
    return mBlob.getFile();
  }

  @Override
  public <T> T toZimbra(Class<T> cls)
  {
    return cls.cast(mBlob);
  }

  @Override
  public String getDigest()
  {
    try
    {
      return mBlob.getDigest();
    }
    catch (IOException e)
    {
      ZimbraLog.mailbox.error(Utils.exceptionToString(e));
      return null;
    }
  }

  public long getSize() throws IOException
  {
    return mBlob.getRawSize();
  }

  @Override
  public String getVolumeId()
  {
    return mVolumeId;
  }

  @Override
  public InputStream getInputStream() throws IOException
  {
    return mBlob.getInputStream();
  }

  @Override
  public boolean hasMailboxInfo()
  {
    return false;
  }

  @Override
  public MailboxBlob toMailboxBlob()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public BlobWrap setDigest(String digest)
  {
    mBlob.setDigest(digest);
    return this;
  }

  @Override
  public BlobWrap setSize(long size)
  {
    mBlob.setRawSize(size);
    return this;
  }

  public String getKey()
  {
    return mBlob.getPath();
  }

  @Override
  public void renameTo(String newPath) throws IOException
  {
    mBlob.renameTo(newPath);
  }

  public static Blob wrapZimbraBlob(Object blob)
  {
    return wrapZimbraBlob(blob, null);
  }

  public static Blob wrapZimbraBlob(Object blob, @Nullable String volumeId)
  {
    // TODO if volumeId == null check if blob instanceof VolumeBlob
    if (blob instanceof Blob)
      return (Blob) blob;

    if (blob instanceof InternalOverrideBlob)
      return ((InternalOverrideBlob) blob).getWrappedObject();

    if (blob instanceof InternalOverrideStagedBlob)
      return ((InternalOverrideStagedBlob) blob).getWrappedObject();

    if (blob instanceof InternalOverrideVolumeBlob)
      return ((InternalOverrideVolumeBlob) blob).getWrappedObject();

    if (blob instanceof VolumeStagedBlob)
      return new BlobWrap(((VolumeStagedBlob) blob).getLocalBlob(), volumeId);

    if (blob instanceof InternalOverrideBlobWithMailboxInfo)
      return ((InternalOverrideBlobWithMailboxInfo) blob).getWrappedObject();

    return new BlobWrap(blob, volumeId);
  }

  @Override
  public String toString()
  {
    return mBlob.toString();
  }
}
