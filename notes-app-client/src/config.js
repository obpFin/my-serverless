const config = {
  MAX_ATTACHMENT_SIZE: 5000000,
  s3: {
    REGION: "eu-west-3",
    BUCKET: "notes-app-obp-upload",
  },
  apiGateway: {
    REGION: "eu-west-3",
    URL: "https://8ro0kaprv4.execute-api.eu-west-3.amazonaws.com/prod",
  },
  cognito: {
    REGION: "eu-west-3",
    USER_POOL_ID: "eu-west-3_Bn1Vnctco",
    APP_CLIENT_ID: "6fkm7rp4gagq0tic8p3kf3nutl",
    IDENTITY_POOL_ID: "eu-west-3:35e77102-d8eb-455e-8441-caa956c971a4",
  },
};

export default config;