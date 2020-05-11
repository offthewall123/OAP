///*
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//package org.apache.spark.sql.execution.datasources.oap.missing;
//
//import org.apache.parquet.compression.CompressionCodecFactory;
//import parquet.column.page.DataPage;
//import parquet.column.page.DictionaryPage;
//import parquet.column.page.PageReader;
//import parquet.io.ParquetDecodingException;
//
//import java.io.IOException;
//import java.util.LinkedList;
//import java.util.List;
//
//public class MissingColumnChunkPageReader {
//    /**
//     * PageReader for a single column chunk. A column chunk contains
//     * several pages, which are yielded one by one in order.
//     *
//     * This implementation is provided with a list of pages, each of which
//     * is decompressed and passed through.
//     */
//    static final class ColumnChunkPageReader implements PageReader {
//
//        private final CompressionCodecFactory.BytesInputDecompressor decompressor;
//        private final long valueCount;
//        private final List<DataPage> compressedPages;
//        private final DictionaryPage compressedDictionaryPage;
//
//        ColumnChunkPageReader(CompressionCodecFactory.BytesInputDecompressor decompressor, List<DataPage> compressedPages, DictionaryPage compressedDictionaryPage) {
//            this.decompressor = decompressor;
//            this.compressedPages = new LinkedList<DataPage>(compressedPages);
//            this.compressedDictionaryPage = compressedDictionaryPage;
//            long count = 0;
//            for (DataPage p : compressedPages) {
//                count += p.getValueCount();
//            }
//            this.valueCount = count;
//        }
//
//        @Override
//        public long getTotalValueCount() {
//            return valueCount;
//        }
//
//        @Override
//        public DataPage readPage() {
//            if (compressedPages.isEmpty()) {
//                return null;
//            }
//            DataPage compressedPage = compressedPages.remove(0);
//            return compressedPage.accept(new DataPage.Visitor<DataPage>() {
//                @Override
//                public DataPage visit(DataPageV1 dataPageV1) {
//                    try {
//                        return new DataPageV1(
//                                decompressor.decompress(dataPageV1.getBytes(), dataPageV1.getUncompressedSize()),
//                                dataPageV1.getValueCount(),
//                                dataPageV1.getUncompressedSize(),
//                                dataPageV1.getStatistics(),
//                                dataPageV1.getRlEncoding(),
//                                dataPageV1.getDlEncoding(),
//                                dataPageV1.getValueEncoding());
//                    } catch (IOException e) {
//                        throw new ParquetDecodingException("could not decompress page", e);
//                    }
//                }
//
//                @Override
//                public DataPage visit(DataPageV2 dataPageV2) {
//                    if (!dataPageV2.isCompressed()) {
//                        return dataPageV2;
//                    }
//                    try {
//                        int uncompressedSize = Ints.checkedCast(
//                                dataPageV2.getUncompressedSize()
//                                        - dataPageV2.getDefinitionLevels().size()
//                                        - dataPageV2.getRepetitionLevels().size());
//                        return DataPageV2.uncompressed(
//                                dataPageV2.getRowCount(),
//                                dataPageV2.getNullCount(),
//                                dataPageV2.getValueCount(),
//                                dataPageV2.getRepetitionLevels(),
//                                dataPageV2.getDefinitionLevels(),
//                                dataPageV2.getDataEncoding(),
//                                decompressor.decompress(dataPageV2.getData(), uncompressedSize),
//                                dataPageV2.getStatistics()
//                        );
//                    } catch (IOException e) {
//                        throw new ParquetDecodingException("could not decompress page", e);
//                    }
//                }
//            });
//        }
//
//        @Override
//        public DictionaryPage readDictionaryPage() {
//            if (compressedDictionaryPage == null) {
//                return null;
//            }
//            try {
//                return new DictionaryPage(
//                        decompressor.decompress(compressedDictionaryPage.getBytes(), compressedDictionaryPage.getUncompressedSize()),
//                        compressedDictionaryPage.getDictionarySize(),
//                        compressedDictionaryPage.getEncoding());
//            } catch (IOException e) {
//                throw new ParquetDecodingException("Could not decompress dictionary page", e);
//            }
//        }
//    }
//}
