{-# LANGUAGE ForeignFunctionInterface #-}
{-# LANGUAGE ScopedTypeVariables #-}

module YAMLStar.FFI
  ( yamlstarLoadFFI
  , yamlstarLoadAllFFI
  , yamlstarDumpFFI
  , yamlstarDumpAllFFI
  , yamlstarVersionFFI
  ) where

import Control.Exception (bracket)
import qualified Data.ByteString as BS
import qualified Data.ByteString.Lazy as LBS
import qualified Data.ByteString.Unsafe as BSU
import Foreign
import Foreign.C.String
import Foreign.C.Types

data GraalCreateIsolateParams
data GraalIsolate
data GraalIsolateThread

type GraalIsolateThreadPtr = Ptr GraalIsolateThread

foreign import ccall "graal_create_isolate"
  c_graal_create_isolate
    :: Ptr GraalCreateIsolateParams
    -> Ptr (Ptr GraalIsolate)
    -> Ptr GraalIsolateThreadPtr
    -> IO CInt

foreign import ccall "graal_tear_down_isolate"
  c_graal_tear_down_isolate :: GraalIsolateThreadPtr -> IO CInt

foreign import ccall "yamlstar_load"
  c_yamlstar_load :: CLLong -> CString -> IO CString

foreign import ccall "yamlstar_load_all"
  c_yamlstar_load_all :: CLLong -> CString -> IO CString

foreign import ccall "yamlstar_dump"
  c_yamlstar_dump :: CLLong -> CString -> IO CString

foreign import ccall "yamlstar_dump_all"
  c_yamlstar_dump_all :: CLLong -> CString -> IO CString

foreign import ccall "yamlstar_version"
  c_yamlstar_version :: CLLong -> IO CString

withGraalIsolate :: (GraalIsolateThreadPtr -> IO a) -> IO a
withGraalIsolate action =
  alloca $ \(isolateThreadPtr :: Ptr GraalIsolateThreadPtr) -> do
    rc <- c_graal_create_isolate nullPtr nullPtr isolateThreadPtr
    if rc /= 0
      then error $ "Failed to create GraalVM isolate (code " ++ show rc ++ ")"
      else do
        isolateThread <- peek isolateThreadPtr
        bracket
          (return isolateThread)
          (\thread -> do
            rc' <- c_graal_tear_down_isolate thread
            if rc' /= 0
              then error $ "Failed to tear down GraalVM isolate (code " ++ show rc' ++ ")"
              else return ())
          action

yamlstarLoadFFI :: BS.ByteString -> IO LBS.ByteString
yamlstarLoadFFI = callWithInput c_yamlstar_load

yamlstarLoadAllFFI :: BS.ByteString -> IO LBS.ByteString
yamlstarLoadAllFFI = callWithInput c_yamlstar_load_all

yamlstarDumpFFI :: BS.ByteString -> IO LBS.ByteString
yamlstarDumpFFI = callWithInput c_yamlstar_dump

yamlstarDumpAllFFI :: BS.ByteString -> IO LBS.ByteString
yamlstarDumpAllFFI = callWithInput c_yamlstar_dump_all

yamlstarVersionFFI :: IO BS.ByteString
yamlstarVersionFFI =
  withGraalIsolate $ \isolateThread -> do
    cResult <- c_yamlstar_version (threadId isolateThread)
    if cResult == nullPtr
      then return BS.empty
      else BS.packCString cResult

callWithInput :: (CLLong -> CString -> IO CString) -> BS.ByteString -> IO LBS.ByteString
callWithInput func input =
  withGraalIsolate $ \isolateThread ->
    BSU.unsafeUseAsCString input $ \cInput -> do
      cResult <- func (threadId isolateThread) cInput
      if cResult == nullPtr
        then return LBS.empty
        else LBS.fromStrict <$> BS.packCString cResult

threadId :: GraalIsolateThreadPtr -> CLLong
threadId = fromIntegral . ptrToIntPtr
