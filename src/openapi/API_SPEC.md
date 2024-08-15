# API

This API utilizes [OpenAPI](https://swagger.io/specification/) to define our `REST` API. This is the file then sent
to `APIGateway`.
`Terraform` is in charge of checking this file for changes, to determine whether it needs to resend the file
to `APIGateway` for redeployment <br>

It is important to note, **NEVER** modify `openapi.yaml` directly. We utilize a `Maven` plugin that builds this file for
you directly every to you rebuild the project.

## How to add a new REST endpoint

1. Add the new endpoint to [path.yaml](./paths/paths.yaml)
2. Create a new file, `foo-new-endpoint.yaml`, inside [resources](./resources) directory that documents your new
   endpoint.
3. Reference this new file inside the [path.yaml](./paths/paths.yaml)
4. Place all request and response schemas inside their respective directory within the [schemas](./schemas) directory
5. Run `mvn clean package` to generate the updated `openapi.yaml` file with your new REST endpoint
