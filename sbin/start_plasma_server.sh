# install arrow and plasma
cd $ROOT_DIR
ARROW_REPO=https://github.com/Intel-bigdata/arrow.git
ARROW_REPO_BRANCH=oap-master
git clone -b $ARROW_REPO_BRANCH $ARROW_REPO arrow
DIR=$ROOT_DIR/arrow
cd $DIR/cpp
rm -rf release
mkdir release
cd release

# build libarrow, libplasma, libplasma_java
cmake -DCMAKE_BUILD_TYPE=Release -DCMAKE_C_FLAGS="-g -O3" -DCMAKE_CXX_FLAGS="-g -O3" -DARROW_BUILD_TESTS=on -DARROW_PLASMA_JAVA_CLIENT=on -DARROW_PLASMA=on -DARROW_DEPENDENCY_SOURCE=BUNDLED ..
make -j40
sudo make install -j40

# copy libs to lib path
sudo rm /lib64/libarrow.so*
sudo cp $DIR/cpp/release/release/libarrow.so.100.0.0 /lib64/
sudo ln -s /lib64/libarrow.so.100.0.0 /lib64/libarrow.so.100
sudo ln -s /lib64/libarrow.so.100 /lib64/libarrow.so

sudo rm /lib64/libplasma.so*
sudo cp $DIR/cpp/release/release/libplasma.so.100.0.0 /lib64/
sudo ln -s /lib64/libplasma.so.100.0.0 /lib64/libplasma.so.100

sudo rm /lib64/libplasma_java.so*
sudo cp $DIR/cpp/release/release/libplasma_java.so /lib64/
sudo ldconfig
