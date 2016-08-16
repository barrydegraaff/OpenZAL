package org.openzal.zal;

import com.zimbra.cs.store.file.VolumeBlobProxy;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

class InternalOverrideVolumeBlob extends VolumeBlobProxy
{
  private final Blob   mBlob;
  private final String mVolumeId;

  public InternalOverrideVolumeBlob(Blob blob)
  {
    super();
    mBlob = blob;
    mVolumeId = blob.getVolumeId();
  }

  public Blob getWrappedObject()
  {
    return mBlob;
  }

  public short getVolumeId()
  {
    return Short.parseShort(mVolumeId);
  }

  @Override
  public File getFile()
  {
    return mBlob.getFile();
  }

  @Override
  public String getPath()
  {
    return mBlob.getKey();
  }

  @Override
  public InputStream getInputStream() throws IOException
  {
    return new FileInputStream(mBlob.getFile());
  }

  @Override
  public boolean isCompressed() throws IOException
  {
    InputStream inputStream = new BufferedInputStream(new FileInputStream(mBlob.getFile()));
    try
    {
      return Utils.isGzipped(inputStream);
    }
    finally
    {
      IOUtils.closeQuietly(inputStream);
    }
  }

  @Override
  public String getDigest() throws IOException
  {
    InputStream inputStream = new FileInputStream(mBlob.getFile());
    try
    {
      return Utils.computeDigest(inputStream);
    }
    finally
    {
      IOUtils.closeQuietly(inputStream);
    }
  }

  @Override
  public long getRawSize() throws IOException
  {
    return mBlob.getSize();
  }

  @Override
  public com.zimbra.cs.store.Blob setCompressed(boolean isCompressed)
  {
    //mBlob.setCompressed(isCompressed);
    return this;
  }

  @Override
  public com.zimbra.cs.store.Blob setDigest(String digest)
  {
    //mBlob.setDigest(digest);
    return this;
  }

  @Override
  public com.zimbra.cs.store.Blob setRawSize(long rawSize)
  {
    //mBlob.setRawSize(rawSize);
    return this;
  }

  @Override
  public com.zimbra.cs.store.Blob copyCachedDataFrom(com.zimbra.cs.store.Blob other)
  {
    //mBlob.copyCachedDataFrom(new BlobWrap(other));
    return this;
  }

  /* $if ZimbraVersion >= 7.1.3 $ */
  public void renameTo(String newPath) throws IOException
  {
    mBlob.renameTo(newPath);
  }
  /* $elseif ZimbraVersion >= 7.0.0 $
  public boolean renameTo(String newPath)
  {
    renameTo(newPath);
    return true;
  }
  /* $elseif ZimbraVersion >= 6.0.15 $
  public void renameTo(String newPath) throws IOException
  {
    mBlob.renameTo(newPath);
  }
  /* $slse $
  public boolean renameTo(String newPath)
  {
    renameTo(newPath);
    return true;
  }
  /* $endif $ */

  @Override
  public String toString()
  {
    return mBlob.toString();
  }

  public static Object wrap(Blob blob)
  {
    if (blob instanceof BlobWrap)
    {
      return blob.toZimbra(com.zimbra.cs.store.Blob.class);
    }
    if (blob instanceof MailboxBlobWrap)
    {
      return InternalOverrideFactory.wrapBlob(((MailboxBlobWrap) blob).getLocalBlob());
    }
    if (blob.hasMailboxInfo())
    {
      return new InternalOverrideBlobWithMailboxInfo(blob);
    }
    if (blob.getVolumeId() != null)
    {
      return new InternalOverrideVolumeBlob(blob);
    }
    else
    {
      return new InternalOverrideBlob(blob);
    }
  }
}