# Release signing

Для выпуска `v1.0.0` через GitHub Actions нужен Android keystore.

## Создать keystore

```powershell
keytool -genkeypair `
  -v `
  -keystore simple-gnss-status-release.keystore `
  -alias simple-gnss-status `
  -keyalg RSA `
  -keysize 2048 `
  -validity 10000
```

Keystore нужно сохранить в надежном месте. Потеря keystore означает, что пользователи не смогут обновиться на APK, подписанный новым ключом.

## Закодировать keystore для GitHub Secrets

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("simple-gnss-status-release.keystore")) | Set-Content keystore-base64.txt
```

## Добавить GitHub Secrets

В репозитории GitHub откройте `Settings` -> `Secrets and variables` -> `Actions` и добавьте:

- `ANDROID_KEYSTORE_BASE64` — содержимое `keystore-base64.txt`;
- `ANDROID_KEYSTORE_PASSWORD` — пароль keystore;
- `ANDROID_KEY_ALIAS` — alias ключа, например `simple-gnss-status`;
- `ANDROID_KEY_PASSWORD` — пароль ключа.

После этого тег `v1.0.0` соберет подписанный release APK и прикрепит его к GitHub Release.
