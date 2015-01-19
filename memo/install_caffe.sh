#! /bin/sh

CAFFE_INSTALL_DIR="ここにインストールしたいディレクトリを書く"
cd "$CAFFE_INSTALL_DIR"
git clone https://github.com/BVLC/caffe
cd "$CAFFE_INSTALL_DIR/caffe"

sudo apt-get -y install libhdf5-dev
sudo apt-get -y install libatlas-base-dev
sudo apt-get -y install python-dev

# python関係                              # <Package Name>
sudo apt-get -y install python            # python
sudo apt-get -y install python-pip        # pip
sudo apt-get -y install cython            # cython
sudo apt-get -y install python-numpy      # numpy
sudo apt-get -y install python-scipy      # scipy
sudo apt-get -y install python-matplotlib # matplotlib
sudo apt-get -y install python-h5py       # h5py
sudo apt-get -y install python-leveldb    # leveldb
sudo apt-get -y install python-networkx   # networkx
sudo apt-get -y install python-nose       # nose
sudo apt-get -y install python-pandas     # pandas
sudo apt-get -y install python-protobuf   # protobuf

sudo pip install scikit-image             # scikit-image
sudo pip install scikit-learn             # scikit-learn
sudo pip install ipython                  # ipython
sudo pip install python-dateutil          # python-dateutil
sudo pip install python-gflags            # python-gflags
sudo pip install pyyaml                   # pyyaml

sudo apt-get -y install libprotobuf-dev libleveldb-dev libsnappy-dev libopencv-dev libboost-all-dev libhdf5-serial-dev
sudo apt-get -y install libgflags-dev libgoogle-glog-dev liblmdb-dev protobuf-compiler

cp Makefile.config.example Makefile.config

echo "この後にやること。"
echo " Makefile.config内の CPU_ONLY := 1 <- コメントを外す"
echo "$ make all -j8"
echo "$ make test"
echo "$ make runtest"
