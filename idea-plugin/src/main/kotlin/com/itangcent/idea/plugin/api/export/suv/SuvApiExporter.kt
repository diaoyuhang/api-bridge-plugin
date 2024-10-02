package com.itangcent.idea.plugin.api.export.suv

import com.fasterxml.jackson.databind.SerializationFeature
import com.google.inject.Inject
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiMethod
import com.itangcent.common.constant.Attrs
import com.itangcent.common.constant.HttpMethod
import com.itangcent.common.dto.ResultDto
import com.itangcent.common.kit.equalIgnoreCase
import com.itangcent.common.kit.fromJson
import com.itangcent.common.kit.toJson
import com.itangcent.common.logger.traceError
import com.itangcent.common.model.Doc
import com.itangcent.common.model.MethodDoc
import com.itangcent.common.model.Request
import com.itangcent.common.utils.filterAs
import com.itangcent.debug.LoggerCollector
import com.itangcent.http.ApacheHttpClient
import com.itangcent.idea.config.CachedResourceResolver
import com.itangcent.idea.plugin.api.ClassApiExporterHelper
import com.itangcent.idea.plugin.api.cache.DefaultFileApiCacheRepository
import com.itangcent.idea.plugin.api.cache.FileApiCacheRepository
import com.itangcent.idea.plugin.api.cache.ProjectCacheRepository
import com.itangcent.idea.plugin.api.export.ExportChannel
import com.itangcent.idea.plugin.api.export.ExportDoc
import com.itangcent.idea.plugin.api.export.core.*
import com.itangcent.idea.plugin.api.export.curl.CurlExporter
import com.itangcent.idea.plugin.api.export.http.HttpClientExporter
import com.itangcent.idea.plugin.api.export.markdown.MarkdownFormatter
import com.itangcent.idea.plugin.api.export.postman.*
import com.itangcent.idea.plugin.api.export.swagger.AnnoInfoAssemble
import com.itangcent.idea.plugin.api.export.swagger.SchemaBuildUtil
import com.itangcent.idea.plugin.config.RecommendConfigReader
import com.itangcent.idea.plugin.config.RemoteServerConfig
import com.itangcent.idea.plugin.dialog.SuvApiExportDialog
import com.itangcent.idea.plugin.rule.SuvRuleParser
import com.itangcent.idea.plugin.settings.SettingBinder
import com.itangcent.idea.plugin.settings.helper.IntelligentSettingsHelper
import com.itangcent.idea.plugin.settings.helper.MarkdownSettingsHelper
import com.itangcent.idea.plugin.settings.helper.PostmanSettingsHelper
import com.itangcent.idea.psi.PsiResource
import com.itangcent.idea.utils.CustomizedPsiClassHelper
import com.itangcent.idea.utils.FileSaveHelper
import com.itangcent.idea.utils.RuleComputeListenerRegistry
import com.itangcent.intellij.config.ConfigReader
import com.itangcent.intellij.config.resource.ResourceResolver
import com.itangcent.intellij.config.rule.RuleComputeListener
import com.itangcent.intellij.config.rule.RuleParser
import com.itangcent.intellij.constant.EventKey
import com.itangcent.intellij.context.ActionContext
import com.itangcent.intellij.extend.findCurrentMethod
import com.itangcent.intellij.extend.guice.singleton
import com.itangcent.intellij.extend.guice.with
import com.itangcent.intellij.extend.withBoundary
import com.itangcent.intellij.file.DefaultLocalFileRepository
import com.itangcent.intellij.file.LocalFileRepository
import com.itangcent.intellij.jvm.PsiClassHelper
import com.itangcent.intellij.jvm.duck.SingleDuckType
import com.itangcent.intellij.logger.Logger
import com.itangcent.intellij.tip.TipsHelper
import com.itangcent.intellij.util.UIUtils
import com.itangcent.suv.http.ConfigurableHttpClientProvider
import com.itangcent.suv.http.HttpClientProvider
import com.itangcent.utils.GitUtils
import io.swagger.v3.oas.models.*
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.BinarySchema
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.parameters.CookieParameter
import io.swagger.v3.oas.models.parameters.HeaderParameter
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.parameters.PathParameter
import io.swagger.v3.oas.models.parameters.QueryParameter
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import io.swagger.v3.oas.models.tags.Tag
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.apache.lucene.util.CollectionUtil
import org.springdoc.core.*
import org.springdoc.core.providers.ObjectMapperProvider
import java.lang.annotation.ElementType
import java.util.*
import kotlin.collections.LinkedHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

open class SuvApiExporter {

    @Inject
    private lateinit var logger: Logger

    @Inject
    private lateinit var actionContext: ActionContext

    @Inject
    private lateinit var classApiExporterHelper: ClassApiExporterHelper

    @Inject
    private lateinit var intelligentSettingsHelper: IntelligentSettingsHelper

    @Suppress("UNCHECKED_CAST")
    fun showExportWindow() {
        LoggerCollector.getLog().takeIf { it.isNotBlank() }?.let { logger.debug(it) }

        try {
            val docs = classApiExporterHelper.export().map { DocWrapper(it) }

            if (docs.isEmpty()) {
                logger.info("No api be found!")
                return
            }

            actionContext.runInSwingUI {

                val multipleApiExportDialog = actionContext!!.instance { SuvApiExportDialog() }

                UIUtils.show(multipleApiExportDialog)

                multipleApiExportDialog.setOnChannelChanged { channel ->
                    if (channel == null) {
                        multipleApiExportDialog.updateRequestListToUI(docs)
                        return@setOnChannelChanged
                    }
                    val apiExporterAdapter = channel as ApiExporterWrapper
                    multipleApiExportDialog.updateRequestListToUI(docs
                        .filter { apiExporterAdapter.support(it.docType) }
                        .toList())
                }

                multipleApiExportDialog.setChannels(EXPORTER_CHANNELS)

                multipleApiExportDialog.setApisHandle { channel, requests ->
                    doExport(channel as ApiExporterWrapper, requests as List<DocWrapper>)
                }


            }
        } catch (e: Exception) {
            logger.traceError("Apis exported failed", e)
        }
    }

    private fun SuvApiExportDialog.updateRequestListToUI(docs: List<DocWrapper>) {
        this.updateRequestList(docs)
        if (intelligentSettingsHelper.selectedOnly()) {
            val currentMethod = actionContext.findCurrentMethod()
            if (currentMethod != null) {
                docs.firstOrNull { it.resourceMethod() == currentMethod }
                    ?.let {
                        this.selectMethod(it)
                        return
                    }
            }
        }
        this.selectAll()
    }

    class DocWrapper {

        var resource: Any?
        var name: String?
        var docType: KClass<*>

        constructor(doc: Doc) {
            this.resource = doc.resource
            this.name = doc.name
            this.docType = doc::class
        }

        constructor(resource: Any?, name: String?, docType: KClass<*>) {
            this.resource = resource
            this.name = name
            this.docType = docType
        }

        override fun toString(): String {
            return name ?: ""
        }
    }

    private fun DocWrapper.resourceMethod(): PsiMethod? {
        return (this.resource as? PsiResource)?.resource() as? PsiMethod
    }

    abstract class ApiExporterAdapter {

        @Inject(optional = true)
        protected var logger: Logger? = null

        @Inject
        protected val classExporter: ClassExporter? = null

        @Inject
        protected lateinit var actionContext: ActionContext

        private var suvApiExporter: SuvApiExporter? = null

        fun setSuvApiExporter(suvApiExporter: SuvApiExporter) {
            this.suvApiExporter = suvApiExporter
        }

        fun exportApisFromMethod(actionContext: ActionContext, requests: List<DocWrapper>) {

            this.logger = actionContext.instance(Logger::class)

            val actionContextBuilder = ActionContext.builder()
            actionContextBuilder.setParentContext(actionContext)
            actionContextBuilder.bindInstance(Project::class, actionContext.instance(Project::class))
            actionContextBuilder.bindInstance(AnActionEvent::class, actionContext.instance(AnActionEvent::class))
            actionContextBuilder.bindInstance(DataContext::class, actionContext.instance(DataContext::class))

            val resources = requests
                .asSequence()
                .map { it.resource }
                .filter { it != null }
                .map { it as PsiResource }
                .map { it.resource() }
                .filter { it is PsiMethod }
                .map { it as PsiMethod }
                .toList()

            actionContextBuilder.bindInstance(MethodFilter::class, ExplicitMethodFilter(resources))

            onBuildActionContext(actionContext, actionContextBuilder)

            val newActionContext = actionContextBuilder.build()

            newActionContext.runAsync {
                try {
                    newActionContext.init(this)
                    beforeExport {
                        newActionContext.runInReadUI {
                            try {
                                doExportApisFromMethod(requests)
                            } catch (e: Exception) {
                                logger!!.error("error to export apis:" + e.message)
                                logger!!.traceError(e)
                            }
                        }
                    }
                } catch (e: Throwable) {
                    logger!!.error("error to export apis:" + e.message)
                    logger!!.traceError(e)
                }
            }

            actionContext.hold()

            newActionContext.on(EventKey.ON_COMPLETED) {
                actionContext.unHold()
            }

            newActionContext.waitCompleteAsync()
        }

        protected open fun beforeExport(next: () -> Unit) {
            next()
        }

        protected open fun onBuildActionContext(
            actionContext: ActionContext,
            builder: ActionContext.ActionContextBuilder
        ) {

            builder.bindInstance("plugin.name", "easy_api")

            builder.inheritFrom(actionContext, SettingBinder::class)

            builder.inheritFrom(actionContext, Logger::class)

            builder.inheritFrom(actionContext, TipsHelper::class)

//            builder.bindInstance(Logger::class, BeanWrapperProxies.wrap(Logger::class, actionContext.instance(Logger::class)))

//            builder.bind(Logger::class) { it.with(ConfigurableLogger::class).singleton() }
//            builder.bind(Logger::class, "delegate.logger") { it.with(ConsoleRunnerLogger::class).singleton() }

            builder.bind(RuleParser::class) { it.with(SuvRuleParser::class).singleton() }
            builder.bind(RuleComputeListener::class) { it.with(RuleComputeListenerRegistry::class).singleton() }
            builder.bind(PsiClassHelper::class) { it.with(CustomizedPsiClassHelper::class).singleton() }

            builder.bind(ResourceResolver::class) { it.with(CachedResourceResolver::class).singleton() }
            builder.bind(FileApiCacheRepository::class) { it.with(DefaultFileApiCacheRepository::class).singleton() }
            builder.bind(LocalFileRepository::class, "projectCacheRepository") {
                it.with(ProjectCacheRepository::class).singleton()
            }

            afterBuildActionContext(actionContext, builder)
        }

        protected open fun actionName(): String {
            return "Basic"
        }

        protected open fun afterBuildActionContext(
            actionContext: ActionContext,
            builder: ActionContext.ActionContextBuilder
        ) {

        }

        private fun doExportApisFromMethod(requestWrappers: List<DocWrapper>) {

            val classes = requestWrappers
                .asSequence()
                .map { it.resource }
                .filter { it is PsiResource }
                .map { it as PsiResource }
                .map { it.resourceClass() }
                .filter { it != null }
                .distinct()
                .toList()


            actionContext.runAsync {
                val docs: MutableList<Doc> = Collections.synchronizedList(ArrayList())

                actionContext.withBoundary {
                    for (cls in classes) {
                        classExporter!!.export(cls!!) { doc ->
                            docs.add(doc)
                        }
                    }
                }

                if (docs.isEmpty()) {
                    logger!!.info("no api has be found")
                }

                doExportDocs(docs)
            }
        }

        abstract fun doExportDocs(docs: MutableList<Doc>)
    }

    class ApiExporterWrapper {
        val adapter: KClass<*>
        val name: String
        private val supportedDocType: Array<KClass<*>>

        constructor(adapter: KClass<*>, name: String, vararg supportedDocTypes: KClass<*>) {
            this.adapter = adapter
            this.name = name
            this.supportedDocType = arrayOf(*supportedDocTypes)
        }

        fun support(docType: KClass<*>): Boolean {
            return this.supportedDocType.contains(docType)
        }

        override fun toString(): String {
            return name
        }
    }

    class ExplicitMethodFilter(private var methods: List<PsiMethod>) : MethodFilter {

        override fun checkMethod(method: PsiMethod): Boolean {
            return this.methods.contains(method)
        }
    }

    open class PostmanApiExporterAdapter : ApiExporterAdapter() {

        @Inject
        private lateinit var postmanApiHelper: PostmanApiHelper

        @Inject
        private lateinit var postmanSettingsHelper: PostmanSettingsHelper

        @Inject
        private val fileSaveHelper: FileSaveHelper? = null

        @Inject
        private val postmanFormatter: PostmanFormatter? = null

        @Inject
        lateinit var project: Project

        override fun actionName(): String {
            return "PostmanExportAction"
        }

        override fun afterBuildActionContext(
            actionContext: ActionContext,
            builder: ActionContext.ActionContextBuilder
        ) {
            super.afterBuildActionContext(actionContext, builder)

            builder.bind(LocalFileRepository::class) { it.with(DefaultLocalFileRepository::class).singleton() }

            builder.bind(PostmanApiHelper::class) { it.with(PostmanCachedApiHelper::class).singleton() }
            builder.bind(HttpClientProvider::class) { it.with(ConfigurableHttpClientProvider::class).singleton() }

            builder.bind(FormatFolderHelper::class) { it.with(PostmanFormatFolderHelper::class).singleton() }

            builder.bind(ConfigReader::class, "delegate_config_reader") {
                it.with(PostmanConfigReader::class).singleton()
            }
            builder.bind(ConfigReader::class) { it.with(RecommendConfigReader::class).singleton() }

            builder.bind(ClassExporter::class) { it.with(CompositeClassExporter::class).singleton() }

            builder.bindInstance(ExportChannel::class, ExportChannel.of("postman"))
            builder.bindInstance(ExportDoc::class, ExportDoc.of("request"))

            builder.bind(RequestBuilderListener::class) { it.with(CompositeRequestBuilderListener::class).singleton() }
            //always not read api from cache
            builder.bindInstance("class.exporter.read.cache", false)

            builder.bindInstance("file.save.default", "postman.json")
            builder.bindInstance("file.save.last.location.key", "com.itangcent.postman.export.path")

        }

        override fun doExportDocs(docs: MutableList<Doc>) {
            actionContext.instance(PostmanApiExporter::class)
                .export(docs.filterAs())
        }
    }

    class ApiPlatformExporterAdapter : PostmanApiExporterAdapter() {

        companion object{
            //搜集的自定义对象完整性标识Map
            val OBJECT_INTEGRITY_MAP = linkedMapOf<String,Boolean>();
        }

        override fun doExportDocs(docs: MutableList<Doc>) {
            try {
                val projectFilePath = project.basePath
                val gitBranchName = GitUtils.getGitBranchName(projectFilePath!!)
                if (StringUtils.isBlank(gitBranchName)) {
                    throw RuntimeException("git branch is empty")
                }

                val openApiList = mutableListOf<OpenAPI>()

                for (request in docs.filterAs<Request>()) {
                    val operation = Operation()
                    val openApi = buildOpenApi("项目名", gitBranchName)
                    openApiList.add(openApi)

                    val someAnnotationsInfo = request.someAnnotationsInfo
                    val methodAnnoInfo = someAnnotationsInfo!![ElementType.METHOD.toString()] as Map<*, *>
                    val classAnnoInfo = someAnnotationsInfo[ElementType.TYPE.toString()] as Map<*, *>

                    if (methodAnnoInfo.contains(Attrs.DEPRECATED_ATTR)) {
                        operation.deprecated = true
                    }
                    setRequestMethodInfo(openApi, request, operation)

                    collectMethodOperationInfo(methodAnnoInfo, operation)
                    collectClassTagInfo(classAnnoInfo, operation, openApi)

                    operation.parameters = collectHeaderParam(request)
                    operation.parameters.addAll(collectPathParam(request))
                    operation.parameters.addAll(collectCookieParam(request))

                    val typeSet = mutableSetOf<String>()
                    operation.parameters.addAll(collectQueryParam(request, typeSet, someAnnotationsInfo))
                    operation.parameters.addAll(collectFormParam(request, typeSet, someAnnotationsInfo))

                    //构建schema
                    val responseContent = setRequestResponseBodySchema(request, operation)
                    collectResponseStatusInfo(methodAnnoInfo, operation, responseContent)
                }
                val openApiMetadata = uploadOpenApiMetaData(openApiList)
                logger!!.info(openApiMetadata.toString())
            } catch (e: Exception) {
                logger!!.traceError("export openapi error", e)
            }
        }

        private fun uploadOpenApiMetaData(openApiList: MutableList<OpenAPI>): MutableList<String> {
            val remoteServerConfig = RemoteServerConfig.getInstance()
            val token = remoteServerConfig.state.configMap["token"]
            val serverId = remoteServerConfig.state.configMap["${project.name}.id"]
            val domain = remoteServerConfig.state.configMap["domain"]

            var tagId = ""
            var tagName = ""
            val apiMeteDateList = mutableListOf<String>()
            for (openApi in openApiList) {
                if (StringUtils.isBlank(token) || StringUtils.isBlank(serverId)) {
                    throw RuntimeException("token or serverId is empty")
                }
                if(CollectionUtils.isEmpty(openApi.tags)){
                    throw RuntimeException("@Tag is null")
                }

                apiMeteDateList.add(writeJson(openApi))
                tagId = openApi.tags[0].description ?: throw RuntimeException("uuid is null")
                tagName = openApi.tags[0].name
            }


            val httpClient = ApacheHttpClient()
            val post =
                httpClient.post("$domain/api/acceptApiMetaDate")
                    .header("token", token)
                    .body(mapOf("apiMeteDateList" to apiMeteDateList,
                        "tagId" to tagId,
                        "tagName" to tagName,
                        "projectId" to serverId).toJson())

            val response = post.call()
            if (200 != response.code()) {
                logger!!.error("上传api信息失败:" + response.string())
            }else{
                val fromJson = response.string()!!.fromJson(ResultDto::class)
                if(200 == fromJson.code){
                    logger!!.info("上传成功")
                }else{
                    logger!!.error("上传失败，${fromJson.msg}")
                }
            }
            return apiMeteDateList
        }

        private fun setRequestResponseBodySchema(
            request: Request,
            operation: Operation
        ): Content {
            //先清空对象完整标识缓存
            OBJECT_INTEGRITY_MAP.clear();
            val requestSchema = SchemaBuildUtil.obtainTypeSchema(request.body, linkedMapOf())

            OBJECT_INTEGRITY_MAP.clear();
            val responseSchema =
                request.response?.get(0)?.let { SchemaBuildUtil.obtainTypeSchema(it.body, linkedMapOf()) }

            val methodConsumes: Array<String> = arrayOf("application/json")
            val methodProduces: Array<String> = arrayOf("*/*")
            val headers: Array<String> = emptyArray()

            val methodAttributes = MethodAttributes(
                "application/json",
                "*/*",
                methodConsumes,
                methodProduces,
                headers,
                Locale.CHINA
            )
            val content = Content()
            val requestBodyInfo = RequestBodyInfo()
            val requestBody = RequestBody()
            requestBodyInfo.requestBody = requestBody
            requestBody.content = content
            if (!request.method.equalIgnoreCase(HttpMethod.GET)) {
                operation.requestBody = requestBody
            }

            for (value in methodAttributes.methodConsumes) {
                val mediaTypeObject = MediaType()
                mediaTypeObject.schema = requestSchema
                content.addMediaType(value, mediaTypeObject)
            }

            val responseContent = Content()
            for (value in methodAttributes.methodProduces) {
                val mediaTypeObject = MediaType()
                mediaTypeObject.schema = responseSchema
                responseContent.addMediaType(value, mediaTypeObject)
            }
            return responseContent
        }

        private fun setRequestMethodInfo(
            openApi: OpenAPI,
            request: Request,
            operation: Operation
        ) {
            val paths: Paths = openApi.paths
            val pathItemObject = PathItem();
            if (HttpMethod.PUT == request.method) {
                pathItemObject.put = operation
            } else if (HttpMethod.POST == request.method) {
                pathItemObject.post = operation
            } else if (HttpMethod.DELETE == request.method) {
                pathItemObject.delete = operation
            } else {
                pathItemObject.get = operation
            }
            paths.addPathItem(request.path?.url(), pathItemObject)
        }

        private fun collectResponseStatusInfo(
            methodAnnoInfo: Map<*, *>,
            operation: Operation,
            responseContent: Content
        ) {
            if (methodAnnoInfo.contains(Attrs.API_RESPONSES_ATTR)) {
                val apiResponses = ApiResponses()
                val apiResponsesInfo = methodAnnoInfo[Attrs.API_RESPONSES_ATTR] as Map<*, *>
                val apiResponseList = apiResponsesInfo["value"] as Array<*>
                operation.responses = apiResponses

                for (objRes in apiResponseList) {
                    val ar = objRes as LinkedHashMap<*, *>

                    val apiResponse = ApiResponse()
                    val httpCode = ar["responseCode"] as String
                    val description = ar["description"] as String
                    apiResponse.description = description
                    apiResponse.content = responseContent
                    apiResponses.addApiResponse(httpCode, apiResponse)
                }
            } else {
                val apiResponses = ApiResponses()
                operation.responses = apiResponses
                val apiResponse = ApiResponse()
                apiResponse.content = responseContent
                apiResponses.addApiResponse("200", apiResponse)
            }
        }

        private fun collectMethodOperationInfo(methodAnnoInfo: Map<*, *>, operation: Operation) {
            if (methodAnnoInfo.contains(Attrs.OPERATION_ATTR)) {
                val operationInfo = methodAnnoInfo[Attrs.OPERATION_ATTR] as Map<*, *>
                operation.summary = operationInfo["summary"] as? String
                operation.description = operationInfo["description"] as? String

            }
        }

        private fun collectClassTagInfo(
            classAnnoInfo: Map<*, *>,
            operation: Operation,
            openApi: OpenAPI
        ) {
            if (classAnnoInfo.contains(Attrs.TAG_ATTR)) {
                val tagInfo = classAnnoInfo[Attrs.TAG_ATTR] as Map<*, *>
                val tagStrList = mutableListOf<String>()
                val name = tagInfo["name"] as String
                val description = tagInfo["description"] as String
                tagStrList.add(name)
                operation.tags = tagStrList
                val tag = Tag()
                tag.name = name
                tag.description = description

                openApi.tags = listOf(tag)
            }
        }

        private fun collectFormParam(
            request: Request,
            typeSet: MutableSet<String>,
            someAnnotationsInfo: LinkedHashMap<String, Any?>?
        ) = request.formParams?.mapNotNull { formParam ->
            val exts = formParam.exts()
            val type = (exts?.get("javaType") as? SingleDuckType)?.canonicalText()
            if (type != null && !typeSet.contains(type)) {
                val param = QueryParameter().apply {
                    val raw = exts["raw"]!!
                    required = formParam.required
                    name = if (raw is LinkedHashMap<*, *>) {
                        schema = SchemaBuildUtil.obtainTypeSchema(raw, linkedMapOf())
                        typeSet.add(type)
                        type
                    } else {
                        schema = SchemaBuildUtil.getTypeSchemaBuild(type).buildSchema(
                            raw, null,
                            linkedMapOf(), type
                        )
                        formParam.name
                    }
                    val fieldAnnoInfo = someAnnotationsInfo!![ElementType.FIELD.toString()] as? Map<*, *> ?: emptyMap<Any,Any>()
                    if (fieldAnnoInfo.contains(formParam.name)) {
                        val fieldMapInfo = fieldAnnoInfo[formParam.name] as LinkedHashMap<String, *>
                        AnnoInfoAssemble.SchemaAnnoAssemble.assembleInfo(schema, fieldMapInfo)
                    }
                }
                param
            } else {
                if ("file" == formParam.type) {
                    val queryParameter = QueryParameter()
                    queryParameter.name=formParam.name
                    queryParameter.required=formParam.required
                    queryParameter.schema = BinarySchema()
                    queryParameter
                }else {
                    null
                }
            }
        } ?: mutableListOf()

        private fun collectQueryParam(
            request: Request,
            typeSet: MutableSet<String>,
            someAnnotationsInfo: LinkedHashMap<String, Any?>?
        ) = request.querys?.mapNotNull { queryParam ->
            val exts = queryParam.exts()
            val type = (exts?.get("javaType") as? SingleDuckType)?.canonicalText()
            if (type != null && !typeSet.contains(type)) {
                val param = QueryParameter().apply {
                    val raw = exts["raw"]!!
                    required = queryParam.required
                    name = if (raw is LinkedHashMap<*, *>) {
                        schema = SchemaBuildUtil.obtainTypeSchema(raw, linkedMapOf())
                        typeSet.add(type)
                        type
                    } else {
                        schema = SchemaBuildUtil.getTypeSchemaBuild(type).buildSchema(
                            raw, null,
                            linkedMapOf(), type
                        )
                        queryParam.name
                    }
                    val fieldAnnoInfo = someAnnotationsInfo!![ElementType.FIELD.toString()] as? Map<*, *> ?: emptyMap<Any,Any>()
                    if (fieldAnnoInfo.contains(queryParam.name)) {
                        val fieldMapInfo = fieldAnnoInfo[queryParam.name] as LinkedHashMap<String, *>
                        AnnoInfoAssemble.SchemaAnnoAssemble.assembleInfo(schema, fieldMapInfo)
                        description = schema.description
                    }
                }
                param
            } else {
                null
            }
        } ?: mutableListOf()

        private fun collectPathParam(request: Request) = request.paths?.map { pathParam ->
            val param = PathParameter().apply {
                name = pathParam.name
                required = true
                val type = (pathParam.exts()?.get("javaType") as SingleDuckType).canonicalText()
                schema = SchemaBuildUtil.getTypeSchemaBuild(type)
                    .buildSchema(pathParam, null, linkedMapOf(), null)
            }
            param
        } ?: mutableListOf()

        private fun collectHeaderParam(request: Request): List<Parameter> =
            request.headers?.mapNotNull { header ->
                if (!header.name.equals("Content-Type")) {
                    val parameter = HeaderParameter().apply {
                        name = header.name
                        required = header.required

                        schema = header.value?.javaClass?.let {
                            SchemaBuildUtil.getTypeSchemaBuild(it.name)
                                .buildSchema(header, null, linkedMapOf(), null)
                        }
                        header.exts()
                    }
                    parameter
                } else {
                    null
                }
            } ?: mutableListOf()

        private fun collectCookieParam(request: Request): List<Parameter> =
            request.cookies?.mapNotNull { cookie ->
                val parameter = CookieParameter().apply {
                    name = cookie.name
                    required = cookie.required

                    schema = cookie.value?.javaClass?.let {
                        SchemaBuildUtil.getTypeSchemaBuild(it.name)
                            .buildSchema(cookie, null, linkedMapOf(), null)
                    }
                    cookie.exts()
                }
                parameter
            } ?: mutableListOf()

        private fun writeJson(openAPI: OpenAPI): String {
            val springDocConfigProperties = SpringDocConfigProperties()
            val objectMapperProvider = ObjectMapperProvider(springDocConfigProperties);
            var jsonMapper = objectMapperProvider.jsonMapper()
            jsonMapper.configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false)

            return jsonMapper.writerFor(openAPI.javaClass).writeValueAsString(openAPI)
        }

        private fun buildOpenApi(title: String, version: String): OpenAPI {
            val openApi = OpenAPI()
            openApi.components = Components()
            openApi.paths = Paths()

            openApi.info = Info().title(title).version(version)
            openApi.servers = mutableListOf()
            return openApi
        }

    }

    class MarkdownApiExporterAdapter : ApiExporterAdapter() {

        @Inject
        private val fileSaveHelper: FileSaveHelper? = null

        @Inject
        private val markdownFormatter: MarkdownFormatter? = null

        @Inject
        private lateinit var markdownSettingsHelper: MarkdownSettingsHelper

        override fun actionName(): String {
            return "MarkdownExportAction"
        }

        override fun afterBuildActionContext(
            actionContext: ActionContext,
            builder: ActionContext.ActionContextBuilder
        ) {
            super.afterBuildActionContext(actionContext, builder)

            builder.bind(LocalFileRepository::class) { it.with(DefaultLocalFileRepository::class).singleton() }

            builder.bind(ClassExporter::class) { it.with(CompositeClassExporter::class).singleton() }

            builder.bindInstance(ExportChannel::class, ExportChannel.of("markdown"))
            builder.bindInstance(ExportDoc::class, ExportDoc.of("request", "methodDoc"))

            builder.bind(ConfigReader::class, "delegate_config_reader") {
                it.with(EasyApiConfigReader::class).singleton()
            }
            builder.bind(ConfigReader::class) { it.with(RecommendConfigReader::class).singleton() }

            //always not read api from cache
            builder.bindInstance("class.exporter.read.cache", false)

            builder.bindInstance("file.save.default", "easy-api.md")
            builder.bindInstance("file.save.last.location.key", "com.itangcent.markdown.export.path")
        }

        override fun doExportDocs(docs: MutableList<Doc>) {
            try {
                if (docs.isEmpty()) {
                    logger!!.info("No api be found to export!")
                    return
                }
                logger!!.info("Start parse apis")
                val apiInfo = markdownFormatter!!.parseRequests(docs)
                docs.clear()
                actionContext.runAsync {
                    try {
                        fileSaveHelper!!.saveOrCopy(apiInfo, markdownSettingsHelper.outputCharset(), {
                            logger!!.info("Exported data are copied to clipboard,you can paste to a md file now")
                        }, {
                            logger!!.info("Apis save success: $it")
                        }) {
                            logger!!.info("Apis save failed")
                        }
                    } catch (e: Exception) {
                        logger!!.traceError("Apis save failed", e)

                    }
                }
            } catch (e: Exception) {
                logger!!.traceError("Apis save failed", e)

            }
        }
    }

    class CurlApiExporterAdapter : ApiExporterAdapter() {

        @Inject
        private lateinit var curlExporter: CurlExporter

        override fun actionName(): String {
            return "CurlExportAction"
        }

        override fun afterBuildActionContext(
            actionContext: ActionContext,
            builder: ActionContext.ActionContextBuilder
        ) {
            super.afterBuildActionContext(actionContext, builder)

            builder.bind(LocalFileRepository::class) { it.with(DefaultLocalFileRepository::class).singleton() }

            builder.bind(ClassExporter::class) { it.with(CompositeClassExporter::class).singleton() }

            builder.bindInstance(ExportDoc::class, ExportDoc.of("request"))

            builder.bind(ConfigReader::class, "delegate_config_reader") {
                it.with(EasyApiConfigReader::class).singleton()
            }
            builder.bind(ConfigReader::class) { it.with(RecommendConfigReader::class).singleton() }

            //always not read api from cache
            builder.bindInstance("class.exporter.read.cache", false)

            builder.bindInstance("file.save.default", "easy-api-curl.md")
            builder.bindInstance("file.save.last.location.key", "com.itangcent.curl.export.path")
        }

        override fun doExportDocs(docs: MutableList<Doc>) {
            val requests = docs.filterAs(Request::class)
            try {
                if (docs.isEmpty()) {
                    logger!!.info("No api be found to export!")
                    return
                }
                curlExporter.export(requests)
            } catch (e: Exception) {
                logger!!.traceError("Apis save failed", e)
            }
        }
    }


    class HttpClientApiExporterAdapter : ApiExporterAdapter() {

        @Inject
        private lateinit var httpClientExporter: HttpClientExporter

        override fun actionName(): String {
            return "HttpClientExportAction"
        }

        override fun afterBuildActionContext(
            actionContext: ActionContext,
            builder: ActionContext.ActionContextBuilder,
        ) {
            super.afterBuildActionContext(actionContext, builder)

            builder.bind(LocalFileRepository::class) { it.with(DefaultLocalFileRepository::class).singleton() }

            builder.bind(ClassExporter::class) { it.with(CompositeClassExporter::class).singleton() }

            builder.bindInstance(ExportDoc::class, ExportDoc.of("request"))

            builder.bind(ConfigReader::class, "delegate_config_reader") {
                it.with(EasyApiConfigReader::class).singleton()
            }
            builder.bind(ConfigReader::class) { it.with(RecommendConfigReader::class).singleton() }

            //always not read api from cache
            builder.bindInstance("class.exporter.read.cache", false)

            builder.bindInstance("file.save.default", "easy-api-httpClient.http")
            builder.bindInstance("file.save.last.location.key", "com.itangcent.httpClient.export.path")
        }

        override fun doExportDocs(docs: MutableList<Doc>) {
            val requests = docs.filterAs(Request::class)
            try {
                if (docs.isEmpty()) {
                    logger!!.info("No api be found to export!")
                    return
                }
                httpClientExporter.export(requests)
            } catch (e: Exception) {
                logger!!.traceError("Apis save failed", e)
            }
        }
    }

    private fun doExport(channel: ApiExporterWrapper, requests: List<DocWrapper>) {
        if (requests.isEmpty()) {
            logger.info("no api has be selected")
            return
        }
        val adapter = channel.adapter.createInstance() as ApiExporterAdapter
        adapter.setSuvApiExporter(this)
        adapter.exportApisFromMethod(actionContext!!, requests)
    }

    companion object {

        private val EXPORTER_CHANNELS: List<*> = listOf(
//            ApiExporterWrapper(PostmanApiExporterAdapter::class, "Postman", Request::class),
//            ApiExporterWrapper(MarkdownApiExporterAdapter::class, "Markdown", Request::class, MethodDoc::class),
//            ApiExporterWrapper(CurlApiExporterAdapter::class, "Curl", Request::class),
//            ApiExporterWrapper(HttpClientApiExporterAdapter::class, "HttpClient", Request::class),
            ApiExporterWrapper(ApiPlatformExporterAdapter::class, "ApiPlatform", Request::class)
        )
    }
}