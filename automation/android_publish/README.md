# Scripts for .apk uploading

## Upload a .apk file to s3 bucket

Example:

```
ansible-playbook android_publish/upload_apk_to_s3.yml -i ec2.py -e "bucket_name=mycomms-android bucket_folder=/MyComms/android/int/`cat BUILD_NUMBER` apk_file=MyComms.apk -v
```
